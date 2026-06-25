package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.RotatedTokens
import com.blueoauld.server.authentication.application.port.RotatedTokenStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisRotatedTokenStore(

    private val redisTemplate: StringRedisTemplate,
) : RotatedTokenStore {

    companion object {
        private const val KEY_PREFIX = "authentication:rotated_token:"
        private const val FIELD_MEMBER_ID = "memberId"
        private const val FIELD_ACCESS_TOKEN = "accessToken"
        private const val FIELD_REFRESH_TOKEN = "refreshToken"
        private val GRACE = Duration.ofSeconds(60)
    }

    override fun save(oldRefreshToken: String, memberId: Long, accessToken: String, refreshToken: String) {
        val key = keyOf(oldRefreshToken)
        redisTemplate.opsForHash<String, String>().putAll(
            key,
            mapOf(
                FIELD_MEMBER_ID to memberId.toString(),
                FIELD_ACCESS_TOKEN to accessToken,
                FIELD_REFRESH_TOKEN to refreshToken,
            ),
        )
        redisTemplate.expire(key, GRACE)
    }

    override fun get(oldRefreshToken: String): RotatedTokens? {
        val entries = redisTemplate.opsForHash<String, String>().entries(keyOf(oldRefreshToken))
        val memberId = entries[FIELD_MEMBER_ID]?.toLongOrNull() ?: return null
        val accessToken = entries[FIELD_ACCESS_TOKEN] ?: return null
        val refreshToken = entries[FIELD_REFRESH_TOKEN] ?: return null

        return RotatedTokens(memberId, accessToken, refreshToken)
    }

    private fun keyOf(token: String) = KEY_PREFIX + token
}
