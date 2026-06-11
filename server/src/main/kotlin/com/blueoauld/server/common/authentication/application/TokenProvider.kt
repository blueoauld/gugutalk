package com.blueoauld.server.common.authentication.application

import com.blueoauld.server.common.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey

@Service
class TokenProvider(

    private val jwtProperties: JwtProperties
) {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun createAccessToken(memberId: Long): String {
        val now = Date()
        val second = Duration.ofSeconds(jwtProperties.accessTokenExpireSeconds)

        return Jwts.builder()
            .subject(memberId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + second.toMillis()))
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(memberId: Long): String {
        val now = Date()
        val second = Duration.ofSeconds(jwtProperties.refreshTokenExpireSeconds)

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