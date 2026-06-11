package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.port.*
import com.blueoauld.server.authentication.application.request.*
import com.blueoauld.server.authentication.application.response.LoginResponse
import com.blueoauld.server.authentication.application.response.RotateTokenResponse
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.common.authentication.application.TokenProvider
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.common.util.IpExtractor
import com.blueoauld.server.common.util.RandomNumberGenerator
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class AuthenticationFacade(

    private val authenticationService: AuthenticationService,
    private val tokenProvider: TokenProvider,
    private val verificationCodeStore: VerificationCodeStore,
    private val verificationSendLimiter: VerificationSendLimiter,
    private val refreshTokenStore: RefreshTokenStore,
    private val accessTokenBlacklistStore: AccessTokenBlacklistStore,
    private val messageSender: MessageSender,
) {

    fun sendVerificationCode(request: SendVerificationCodeRequest, servletRequest: HttpServletRequest) {
        if (verificationSendLimiter.isExceeded(request.deviceId)) {
            throw CustomException(ErrorCode.VERIFICATION_CODE_01)
        }

        val code = RandomNumberGenerator.generateSixDigitCode()
        val ip = IpExtractor.extract(servletRequest)

        // DB
        authenticationService.saveVerificationCode(request, ip, code)

        // Redis
        verificationCodeStore.save(request.phone, code)
        verificationSendLimiter.record(request.deviceId)

        // SMS
        messageSender.send(request.phone, code)
    }

    fun signup(request: SignupRequest): SignupResponse {
        if (verificationCodeStore.get(request.phone) != request.verificationCode) {
            throw CustomException(ErrorCode.VERIFICATION_CODE_02)
        }
        if (request.password != request.confirmPassword) {
            throw CustomException(ErrorCode.MEMBER_04)
        }

        // DB
        val member = authenticationService.createMember(request)

        // Redis
        val accessToken = tokenProvider.createAccessToken(member.id)
        val refreshToken = tokenProvider.createRefreshToken(member.id)

        refreshTokenStore.save(member.id, refreshToken)
        verificationCodeStore.delete(request.phone)

        return SignupResponse(member.id, accessToken, refreshToken)
    }

    fun setup(memberId: Long, request: SetupRequest) {
        // DB
        authenticationService.updateProfile(memberId, request)
    }

    fun login(request: LoginRequest): LoginResponse {
        // DB
        val member = authenticationService.authenticate(request)

        // Redis
        val accessToken = tokenProvider.createAccessToken(member.id)
        val refreshToken = tokenProvider.createRefreshToken(member.id)

        refreshTokenStore.save(member.id, refreshToken)
        return LoginResponse(member.id, accessToken, refreshToken)
    }

    fun logout(memberId: Long, request: LogoutRequest) {
        // Redis
        val foundMemberId = refreshTokenStore.getMemberId(request.refreshToken)

        if (foundMemberId == null || memberId != foundMemberId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_02)
        }

        accessTokenBlacklistStore.save(memberId, request.accessToken)
        refreshTokenStore.delete(request.refreshToken)
    }

    fun rotateToken(request: RotateTokenRequest): RotateTokenResponse {
        // DB
        val member = authenticationService.getMember(request.memberId)

        // Redis
        val foundMemberId = refreshTokenStore.getMemberId(request.refreshToken)

        if (foundMemberId == null || request.memberId != foundMemberId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_02)
        }

        accessTokenBlacklistStore.save(request.memberId, request.accessToken)
        refreshTokenStore.delete(request.refreshToken)

        val accessToken = tokenProvider.createAccessToken(member.id)
        val refreshToken = tokenProvider.createRefreshToken(member.id)

        refreshTokenStore.save(member.id, refreshToken)

        return RotateTokenResponse(
            memberId = member.id,
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun deleteAccount(memberId: Long) {
        // DB
        authenticationService.deleteMember(memberId)
    }
}