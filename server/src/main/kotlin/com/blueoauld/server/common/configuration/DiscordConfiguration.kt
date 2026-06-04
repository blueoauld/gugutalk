package com.blueoauld.server.common.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord")
data class DiscordProperties(

    val botToken: String,
    val guildId: String,
)