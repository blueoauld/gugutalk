package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.request.SendVerificationCodeRequest
import com.blueoauld.server.authentication.entity.VerificationCode
import com.blueoauld.server.authentication.repository.VerificationCodeRepository
import com.blueoauld.server.common.util.IpExtractor
import com.blueoauld.server.common.util.RandomNumberGenerator
import com.blueoauld.server.member.repository.MemberRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private const val AUTHENTICATION_PHONE_CODE_KEY = "authentication:phone:code:"
private const val AUTHENTICATION_DEVICE_ID_COUNT_KEY = "authentication:device_id:count:"

@Service
class AuthenticationService(

    private val memberRepository: MemberRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val stringRedisTemplate: StringRedisTemplate
) {

    @Transactional
    fun sendVerificationCode(request: SendVerificationCodeRequest, servletRequest: HttpServletRequest) {
        val countKey = AUTHENTICATION_DEVICE_ID_COUNT_KEY + request.deviceId
        val count = stringRedisTemplate.opsForValue().get(countKey)?.toInt() ?: 0

        if (count >= 3) {
            throw IllegalArgumentException("인증번호 요청 횟수를 초과했습니다.")
        }

        val codeKey = AUTHENTICATION_PHONE_CODE_KEY + request.phone
        val digitCode = RandomNumberGenerator.generateSixDigitCode()

        stringRedisTemplate.opsForValue().set(codeKey, digitCode, 3, TimeUnit.MINUTES)
        stringRedisTemplate.opsForValue().increment(countKey)

        if (count == 0) {
            stringRedisTemplate.expire(countKey, getSecondsUntilMidnight(), TimeUnit.SECONDS)
        }

        val verificationCode = VerificationCode(
            phone = request.phone,
            deviceId = request.deviceId,
            ip = IpExtractor.extract(servletRequest),
            code = digitCode,
        )
        verificationCodeRepository.save(verificationCode)
    }

    private fun getSecondsUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()

        return ChronoUnit.SECONDS.between(now, midnight)
    }
}