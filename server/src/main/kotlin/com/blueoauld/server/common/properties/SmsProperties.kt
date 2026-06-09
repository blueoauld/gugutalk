package com.blueoauld.server.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "sms")
data class SmsProperties(

    val phone: String,
    val apiKey: String,
    val apiSecret: String,
)
