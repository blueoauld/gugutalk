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
        private const val MEMBER_KEY_PREFIX = "point:attendance:member:"
        private const val DEVICE_KEY_PREFIX = "point:attendance:device:"
        private val ZONE = ZoneId.of("Asia/Seoul")
    }

    override fun claim(memberId: Long, deviceId: String): Boolean {
        val ttl = nextMidnight()
        val memberKey = memberKeyOf(memberId)
        val deviceKey = deviceKeyOf(deviceId)

        if (redisTemplate.opsForValue().setIfAbsent(memberKey, memberId.toString(), ttl) != true) {
            return false
        }
        if (redisTemplate.opsForValue().setIfAbsent(deviceKey, memberId.toString(), ttl) != true) {
            redisTemplate.delete(memberKey)
            return false
        }

        registerRollbackCleanup(memberKey, deviceKey)
        return true
    }

    override fun isAttend(memberId: Long, deviceId: String): Boolean =
        redisTemplate.countExistingKeys(
            listOf(memberKeyOf(memberId), deviceKeyOf(deviceId))
        ) > 0

    private fun registerRollbackCleanup(vararg keys: String) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return
        }

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    redisTemplate.delete(keys.toList())
                }
            }
        })
    }

    private fun memberKeyOf(memberId: Long) = MEMBER_KEY_PREFIX + memberId
    private fun deviceKeyOf(deviceId: String) = DEVICE_KEY_PREFIX + deviceId

    private fun nextMidnight(): Duration {
        val now = ZonedDateTime.now(ZONE)
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay(ZONE)
        return Duration.between(now, midnight)
    }
}