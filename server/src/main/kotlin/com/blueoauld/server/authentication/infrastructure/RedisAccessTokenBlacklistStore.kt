package com.blueoauld.server.authentication.infrastructure

import com.blueoauld.server.authentication.application.port.AccessTokenBlacklistStore
import com.blueoauld.server.common.properties.JwtProperties
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RedisAccessTokenBlacklistStore(

    private val redisTemplate: StringRedisTemplate,
    private val jwtProperties: JwtProperties,
) : AccessTokenBlacklistStore {

    companion object {
        private const val KEY_PREFIX = "authentication:access_token:blacklist:"
    }

    override fun save(memberId: Long, token: String) {
        redisTemplate.opsForValue().set(
            keyOf(token),
            memberId.toString(),
            Duration.ofSeconds(jwtProperties.accessTokenExpireSeconds),
        )
    }

    override fun contain(token: String): Boolean = redisTemplate.hasKey(KEY_PREFIX + token)

    private fun keyOf(token: String) = KEY_PREFIX + token
}