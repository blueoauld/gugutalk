package com.blueoauld.server.admin.application

import com.blueoauld.server.ban.application.BanService
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.properties.DiscordProperties
import com.blueoauld.server.discord.infrastructure.SlashCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class AdminMemberBanDeleteCommand(

    private val banService: BanService,
    private val discordProperties: DiscordProperties,
) : SlashCommand {

    private val log = KotlinLogging.logger {}

    override val name: String = "정지해제"

    override fun data(): SlashCommandData = Commands.slash(name, "회원의 정지를 해제합니다.")
        .addOptions(
            OptionData(OptionType.STRING, "uuid", "정지 UUID", true),
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val uuid = event.getOption("uuid")?.asString

        if (uuid.isNullOrBlank()) {
            event.reply("UUID를 입력해주시길 바랍니다.").setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        try {
            banService.delete(uuid)
        } catch (e: CustomException) {
            event.hook.sendMessage(e.errorCode.message).queue()
            return
        }

        val channel = event.jda.getTextChannelById(discordProperties.banChannelId)
        if (channel == null) {
            log.warn { "정지 채널을 찾을 수 없습니다. 채널 ID = ${discordProperties.banChannelId}" }
            return
        }

        event.hook.sendMessage("정지가 해제되었습니다.").queue()
    }
}