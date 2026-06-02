package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.VerificationCodeStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisVerificationCodeStore(

    private val redisTemplate: StringRedisTemplate
) : VerificationCodeStore {

    companion object {
        private const val KEY_PREFIX = "authentication:phone_code:"
        private const val CODE_TTL_MINUTES = 10L
    }

    override fun save(phone: String, code: String) {
        redisTemplate.opsForValue().set(
            keyOf(phone),
            code,
            Duration.ofMinutes(CODE_TTL_MINUTES)
        )
    }

    override fun get(phone: String): String? = redisTemplate.opsForValue().get(keyOf(phone))

    override fun delete(phone: String) {
        redisTemplate.delete(keyOf(phone))
    }

    private fun keyOf(phone: String) = KEY_PREFIX + phone
}