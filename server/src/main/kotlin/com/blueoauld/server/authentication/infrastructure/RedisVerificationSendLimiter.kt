package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.VerificationSendLimiter
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Repository
class RedisVerificationSendLimiter(

    private val redisTemplate: StringRedisTemplate,
) : VerificationSendLimiter {

    companion object {
        private const val KEY_PREFIX = "authentication:device_send_count:"
        private const val MAX_PER_DAY = 3
    }

    override fun isExceeded(deviceId: String): Boolean {
        val count = redisTemplate.opsForValue().get(keyOf(deviceId))?.toInt() ?: 0
        return count >= MAX_PER_DAY
    }

    override fun record(deviceId: String) {
        val key = keyOf(deviceId)
        val count = redisTemplate.opsForValue().increment(key) ?: 1L

        if (count == 1L) {
            redisTemplate.expireAt(key, nextMidnight())
        }
    }

    private fun keyOf(deviceId: String) = KEY_PREFIX + deviceId

    private fun nextMidnight(): Instant = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
}