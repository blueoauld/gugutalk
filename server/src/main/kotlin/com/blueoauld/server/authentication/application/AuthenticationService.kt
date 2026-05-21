package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.request.SendVerificationCodeRequest
import com.blueoauld.server.authentication.application.request.SetupRequest
import com.blueoauld.server.authentication.application.request.SignupRequest
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.authentication.entity.VerificationCode
import com.blueoauld.server.authentication.repository.VerificationCodeRepository
import com.blueoauld.server.common.authentication.application.TokenProvider
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

@Service
class AuthenticationService(

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
            throw IllegalArgumentException("인증번호 요청 횟수를 초과했습니다.")
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
            throw IllegalArgumentException("인증 번호를 다시 한번 확인해주시길 바랍니다.")
        }
        if (memberRepository.existsByPhone(request.phone)) {
            throw IllegalArgumentException("이미 가입된 휴대폰 번호입니다.")
        }
        if (request.password != request.confirmPassword) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
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

        return SignupResponse(accessToken, refreshToken)
    }

    @Transactional
    fun setup(memberId: Long, request: SetupRequest) {
        val member = (memberRepository.findByIdOrNull(memberId)
            ?: throw IllegalArgumentException("존재하지 않는 회원입니다."))

        if (memberRepository.existsByNickname(request.nickname)) {
            throw IllegalArgumentException("이미 사용중인 닉네임입니다.")
        }

        member.setup(
            nickname = request.nickname,
            birthYear = request.birthYear,
            region = request.region,
            bio = request.bio
        )
    }

    private fun getSecondsUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()

        return ChronoUnit.SECONDS.between(now, midnight)
    }
}