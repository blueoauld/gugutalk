package com.blueoauld.server.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "apns")
data class ApnsProperties(

    val teamId: String,
    val keyId: String,
    val bundleId: String,
    val keyPath: String,
    val production: Boolean = false,
)
