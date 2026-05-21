package com.blueoauld.server.common.authentication.application

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey

@Service
class TokenProvider(

    @Value($$"${jwt.secret}") private val secret: String,
    @Value($$"${jwt.access-token-expire-seconds}") private val accessTokenExpireSeconds: Long,
    @Value($$"${jwt.refresh-token-expire-seconds}") private val refreshTokenExpireSeconds: Long,
) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun createAccessToken(memberId: Long, nickname: String): String {
        val now = Date()
        val second = Duration.ofSeconds(accessTokenExpireSeconds)

        return Jwts.builder()
            .subject(memberId.toString())
            .claim("nickname", nickname)
            .issuedAt(now)
            .expiration(Date(now.time + second.toMillis()))
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(memberId: Long): String {
        val now = Date()
        val second = Duration.ofSeconds(refreshTokenExpireSeconds)

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + second.toMillis()))
            .signWith(key)
            .compact()
    }

    fun parseAndValidate(token: String): Long? = runCatching {
        val claims = parseClaims(token)
        claims.subject.toLong()
    }.getOrNull()

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}