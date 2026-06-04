package com.blueoauld.server.common.configuration

import com.blueoauld.server.common.properties.DiscordProperties
import com.blueoauld.server.discord.infrastructure.CommandDispatcher
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JdaConfig(

    private val discordProperties: DiscordProperties,
    private val commandDispatcher: CommandDispatcher,
) {

    @Bean(destroyMethod = "shutdown")
    fun jda(): JDA {
        val jda = JDABuilder.createLight(discordProperties.botToken, emptyList())
            .addEventListeners(commandDispatcher)
            .build()
            .awaitReady()

        val commandData = commandDispatcher.allCommands.map { it.data() }

        jda.getGuildById(discordProperties.guildId)
            ?.updateCommands()
            ?.addCommands(commandData)
            ?.queue()

        return jda
    }
}