package com.blueoauld.server.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cloudflare.r2")
data class R2Properties(

    val accountId: String,
    val accessKey: String,
    val secretKey: String,
    val api: String,
    val domain: String,
)