package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.RefreshTokenStore
import com.blueoauld.server.common.properties.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisRefreshTokenStore(

    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties,
) : RefreshTokenStore {

    companion object {
        private const val KEY_PREFIX = "authentication:refresh_token:"
    }

    override fun save(memberId: Long, token: String) {
        redisTemplate.opsForValue().set(
            keyOf(token),
            memberId.toString(),
            Duration.ofSeconds(jwtProperties.refreshTokenExpireSeconds),
        )
    }

    private fun keyOf(token: String) = KEY_PREFIX + token
}