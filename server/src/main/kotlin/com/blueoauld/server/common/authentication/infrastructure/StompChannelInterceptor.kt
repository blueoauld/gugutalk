package com.blueoauld.server.common.authentication.infrastructure

import com.blueoauld.server.authentication.application.AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY
import com.blueoauld.server.common.authentication.application.TokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class StompChannelInterceptor(

    private val tokenProvider: TokenProvider,
    private val stringRedisTemplate: StringRedisTemplate
) : ChannelInterceptor {

    private val log = KotlinLogging.logger {}

    override fun preSend(
        message: Message<*>,
        channel: MessageChannel
    ): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message

        if (accessor.command == StompCommand.CONNECT) {
            val accessToken = resolveAccessToken(accessor)
            val memberId = accessToken?.let { tokenProvider.parseAndValidate(it) }

            if (accessToken == null || memberId == null) {
                throw MessageDeliveryException(message, "유효하지 않은 토큰입니다.")
            }

            val accessTokenBlacklistKey = AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY + accessToken
            if (stringRedisTemplate.hasKey(accessTokenBlacklistKey)) {
                throw MessageDeliveryException(message, "이미 로그아웃된 토큰입니다.")
            }
            accessor.setUser { memberId.toString() }

            log.info { "STOMP = CONNECT, 회원 ID = $memberId" }
        }
        return message
    }

    private fun resolveAccessToken(accessor: StompHeaderAccessor): String? {
        val authorizationHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (authorizationHeader.startsWith("Bearer ")) authorizationHeader.substring(7) else null
    }
}