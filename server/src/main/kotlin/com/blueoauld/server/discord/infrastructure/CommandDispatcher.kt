package com.blueoauld.server.discord.infrastructure

import com.blueoauld.server.common.configuration.DiscordProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class CommandDispatcher(

    commands: List<SlashCommand>,
    private val discordProperties: DiscordProperties,
) : ListenerAdapter() {

    private val log = KotlinLogging.logger {}
    private val commandMap: Map<String, SlashCommand> = commands.associateBy { it.name }

    val allCommands: List<SlashCommand> = commands

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.guild?.id != discordProperties.guildId) {
            event.reply("사용할 수 없는 서버입니다.").setEphemeral(true).queue()
            return
        }

        val command = commandMap[event.name]
        if (command == null) {
            event.reply("존재하지 않는 명령어입니다.").setEphemeral(true).queue()
            return
        }

        try {
            command.execute(event)
        } catch (e: Exception) {
            log.error(e) { "명령어 '${event.name}' 처리 중 오류" }

            if (event.isAcknowledged) {
                event.hook.sendMessage("처리 중 오류가 발생했습니다.").setEphemeral(true).queue()
            } else {
                event.reply("처리 중 오류가 발생했습니다.").setEphemeral(true).queue()
            }
        }
    }
}