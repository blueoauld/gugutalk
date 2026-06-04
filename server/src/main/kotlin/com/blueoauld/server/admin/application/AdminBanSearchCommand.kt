package com.blueoauld.server.admin.application

import com.blueoauld.server.ban.application.BanService
import com.blueoauld.server.ban.application.response.BanGetResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.discord.infrastructure.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class AdminBanSearchCommand(

    private val banService: BanService
) : SlashCommand {

    override val name: String = "정지조회"

    override fun data(): SlashCommandData = Commands.slash(name, "회원 정지 정보를 조회합니다.")
        .addOption(OptionType.STRING, "uuid", "정지 UUID", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val uuid = event.getOption("uuid")?.asString
        if (uuid.isNullOrBlank()) {
            event.reply("UUID를 입력해주시길 바랍니다.").setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        val response = try {
            banService.get(uuid)
        } catch (e: CustomException) {
            event.hook.sendMessage(e.errorCode.message).queue()
            return
        }

        event.hook.sendMessageEmbeds(buildEmbed(response)).queue()
    }

    private fun buildEmbed(response: BanGetResponse): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("정지")
            .addField("ID", "`${response.banId}`", false)
            .addField("UUID", "`${response.uuid}`", false)
            .addField("항목", response.type.name, false)
            .addField("타겟", "`${response.target}`", false)
            .addField("정지 일수", "${response.days}일", false)
            .addField("사유", response.reason, false)
            .addField("정지일", "<t:${response.createdAt.epochSecond}:f>", false)
            .addField("해제일", "<t:${response.expiredAt.epochSecond}:f>", false)

        return builder.build()
    }
}