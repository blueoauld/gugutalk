package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.port.AccessTokenBlacklistStore
import com.blueoauld.server.authentication.application.port.RefreshTokenStore
import com.blueoauld.server.authentication.application.port.VerificationCodeStore
import com.blueoauld.server.authentication.application.port.VerificationSendLimiter
import com.blueoauld.server.authentication.application.request.*
import com.blueoauld.server.authentication.application.response.LoginResponse
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.authentication.entity.VerificationCode
import com.blueoauld.server.authentication.repository.VerificationCodeRepository
import com.blueoauld.server.common.authentication.application.TokenProvider
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.util.IpExtractor
import com.blueoauld.server.common.util.RandomNumberGenerator
import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.repository.MemberRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY = "authentication:access_token:blacklist:"

@Service
class AuthenticationService(

    private val memberRepository: MemberRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val verificationCodeStore: VerificationCodeStore,
    private val verificationSendLimiter: VerificationSendLimiter,
    private val refreshTokenStore: RefreshTokenStore,
    private val accessTokenBlacklistStore: AccessTokenBlacklistStore,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun sendVerificationCode(request: SendVerificationCodeRequest, servletRequest: HttpServletRequest) {
        if (verificationSendLimiter.isExceeded(request.deviceId)) {
            throw CustomException(VERIFICATION_CODE_01)
        }

        val code = RandomNumberGenerator.generateSixDigitCode()

        verificationCodeRepository.save(
            VerificationCode(
                phone = request.phone,
                deviceId = request.deviceId,
                ip = IpExtractor.extract(servletRequest),
                code = code,
            )
        )

        verificationCodeStore.save(request.phone, code)
        verificationSendLimiter.record(request.deviceId)
    }

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (verificationCodeStore.get(request.phone) != request.verificationCode) {
            throw CustomException(VERIFICATION_CODE_02)
        }
        if (request.password != request.confirmPassword) {
            throw CustomException(MEMBER_04)
        }
        if (memberRepository.existsByPhone(request.phone)) {
            throw CustomException(MEMBER_02)
        }

        val member = Member(
            phone = request.phone,
            password = passwordEncoder.encode(request.password)!!,
            deviceId = request.deviceId,
            gender = request.gender
        )
        memberRepository.save(member)

        val accessToken = tokenProvider.createAccessToken(member.id, member.nickname)
        val refreshToken = tokenProvider.createRefreshToken(member.id)

        refreshTokenStore.save(member.id, refreshToken)
        verificationCodeStore.delete(request.phone)
        return SignupResponse(member.id, accessToken, refreshToken)
    }

    @Transactional
    fun setup(memberId: Long, request: SetupRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)

        if (memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        member.updateProfile(
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio
        )
    }

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val member = (memberRepository.findByPhone(request.phone) ?: throw CustomException(MEMBER_05))

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw CustomException(MEMBER_05)
        }

        member.updateDeviceId(request.deviceId)

        val accessToken = tokenProvider.createAccessToken(member.id, member.nickname)
        val refreshToken = tokenProvider.createRefreshToken(member.id)

        refreshTokenStore.save(member.id, refreshToken)
        return LoginResponse(member.id, accessToken, refreshToken)
    }

    @Transactional(readOnly = true)
    fun logout(memberId: Long, request: LogoutRequest) {
        val foundMemberId = refreshTokenStore.getMemberId(request.refreshToken)

        if (foundMemberId == null || memberId != foundMemberId) {
            throw CustomException(UNAUTHORIZED_02)
        }

        accessTokenBlacklistStore.save(memberId, request.accessToken)
        refreshTokenStore.delete(request.refreshToken)
    }
}