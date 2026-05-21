package com.blueoauld.server.authentication.application

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
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private const val AUTHENTICATION_PHONE_CODE_KEY = "authentication:phone:code:"
private const val AUTHENTICATION_DEVICE_ID_COUNT_KEY = "authentication:device_id:count:"
private const val AUTHENTICATION_REFRESH_TOKEN_KEY = "authentication:refresh_token:"

const val AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY = "authentication:access_token:blacklist:"

@Service
class AuthenticationService(

    @Value($$"${jwt.access-token-expire-seconds}") private val accessTokenExpireSeconds: Long,
    @Value($$"${jwt.refresh-token-expire-seconds}") private val refreshTokenExpireSeconds: Long,

    private val memberRepository: MemberRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val stringRedisTemplate: StringRedisTemplate,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun sendVerificationCode(request: SendVerificationCodeRequest, servletRequest: HttpServletRequest) {
        val countKey = AUTHENTICATION_DEVICE_ID_COUNT_KEY + request.deviceId
        val count = stringRedisTemplate.opsForValue().get(countKey)?.toInt() ?: 0

        if (count >= 3) {
            throw CustomException(VERIFICATION_CODE_01)
        }

        val digitCode = RandomNumberGenerator.generateSixDigitCode()

        val verificationCode = VerificationCode(
            phone = request.phone,
            deviceId = request.deviceId,
            ip = IpExtractor.extract(servletRequest),
            code = digitCode,
        )
        verificationCodeRepository.save(verificationCode)

        val codeKey = AUTHENTICATION_PHONE_CODE_KEY + request.phone

        stringRedisTemplate.opsForValue().set(codeKey, digitCode, 3, TimeUnit.MINUTES)
        stringRedisTemplate.opsForValue().increment(countKey)

        if (count == 0) {
            stringRedisTemplate.expire(countKey, getSecondsUntilMidnight(), TimeUnit.SECONDS)
        }
    }

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        val codeKey = AUTHENTICATION_PHONE_CODE_KEY + request.phone

        if (stringRedisTemplate.opsForValue().get(codeKey) != request.verificationCode) {
            throw CustomException(VERIFICATION_CODE_02)
        }
        if (memberRepository.existsByPhone(request.phone)) {
            throw CustomException(MEMBER_02)
        }
        if (request.password != request.confirmPassword) {
            throw CustomException(MEMBER_04)
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

        val refreshTokenKey = AUTHENTICATION_REFRESH_TOKEN_KEY + refreshToken

        stringRedisTemplate.opsForValue().set(
            refreshTokenKey, member.id.toString(), refreshTokenExpireSeconds, TimeUnit.SECONDS
        )
        stringRedisTemplate.delete(codeKey)

        return SignupResponse(member.id, accessToken, refreshToken)
    }

    @Transactional
    fun setup(memberId: Long, request: SetupRequest) {
        val member = (memberRepository.findByIdOrNull(memberId)
            ?: throw CustomException(MEMBER_01))

        if (memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(MEMBER_03)
        }

        member.setup(
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

        val refreshTokenKey = AUTHENTICATION_REFRESH_TOKEN_KEY + refreshToken

        stringRedisTemplate.opsForValue().set(
            refreshTokenKey, member.id.toString(), refreshTokenExpireSeconds, TimeUnit.SECONDS
        )
        return LoginResponse(member.id, accessToken, refreshToken)
    }

    @Transactional(readOnly = true)
    fun logout(memberId: Long, request: LogoutRequest) {
        if (!memberRepository.existsById(memberId)) {
            throw CustomException(MEMBER_01)
        }

        val refreshTokenKey = AUTHENTICATION_REFRESH_TOKEN_KEY + request.refreshToken
        val value = stringRedisTemplate.opsForValue().get(refreshTokenKey)

        if (value == null || value != memberId.toString()) {
            throw CustomException(UNAUTHORIZED_02)
        }

        val accessTokenBlacklistKey = AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY + request.accessToken
        stringRedisTemplate.opsForValue().set(
            accessTokenBlacklistKey, memberId.toString(), accessTokenExpireSeconds, TimeUnit.SECONDS
        )
        stringRedisTemplate.delete(refreshTokenKey)
    }

    private fun getSecondsUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()

        return ChronoUnit.SECONDS.between(now, midnight)
    }
}