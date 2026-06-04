package com.blueoauld.server.common.configuration

import com.blueoauld.server.discord.infrastructure.CommandDispatcher
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JdaConfig(

    private val properties: DiscordProperties,
    private val dispatcher: CommandDispatcher,
) {

    @Bean(destroyMethod = "shutdown")
    fun jda(): JDA {
        val jda = JDABuilder.createLight(properties.botToken, emptyList())
            .addEventListeners(dispatcher)
            .build()
            .awaitReady()

        val commandData = dispatcher.allCommands.map { it.data() }

        jda.getGuildById(properties.guildId)
            ?.updateCommands()
            ?.addCommands(commandData)
            ?.queue()

        return jda
    }
}