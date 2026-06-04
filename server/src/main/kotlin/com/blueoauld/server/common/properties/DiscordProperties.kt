package com.blueoauld.server.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord")
data class DiscordProperties(

    val botToken: String,
    val guildId: String,
    val reportChannelId: String,
    val banChannelId: String,
)