package com.blueoauld.server.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(

    val secret: String,
    val accessTokenExpireSeconds: Long,
    val refreshTokenExpireSeconds: Long,
)
