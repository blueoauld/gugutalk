package com.blueoauld.server.point.infrastructure

import com.blueoauld.server.point.application.port.AttendanceStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class RedisAttendanceStore(

    private val redisTemplate: StringRedisTemplate
) : AttendanceStore {

    companion object {
        private const val KEY_PREFIX = "point:attendance:"
        private val ZONE = ZoneId.of("Asia/Seoul")
    }

    override fun isAttend(memberId: Long): Boolean = redisTemplate.hasKey(keyOf(memberId))

    override fun mark(memberId: Long) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                redisTemplate.opsForValue().set(
                    keyOf(memberId),
                    memberId.toString(),
                    nextMidnight()
                )
            }
        })
    }

    private fun keyOf(memberId: Long) = KEY_PREFIX + memberId

    private fun nextMidnight(): Duration {
        val now = ZonedDateTime.now(ZONE)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(ZONE)
        return Duration.between(now, midnight)
    }
}