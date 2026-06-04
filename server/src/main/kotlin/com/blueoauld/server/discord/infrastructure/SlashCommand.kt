package com.blueoauld.server.discord.infrastructure

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommand {

    val name: String

    fun data(): SlashCommandData

    fun execute(event: SlashCommandInteractionEvent)
}