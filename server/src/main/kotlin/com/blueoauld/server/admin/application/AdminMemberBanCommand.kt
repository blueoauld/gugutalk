package com.blueoauld.server.admin.application

import com.blueoauld.server.admin.application.type.BanTarget
import com.blueoauld.server.ban.application.BanService
import com.blueoauld.server.ban.application.request.BanCreateRequest
import com.blueoauld.server.ban.entity.type.BanType
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.properties.DiscordProperties
import com.blueoauld.server.discord.infrastructure.SlashCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class AdminMemberBanCommand(

    private val banService: BanService,
    private val discordProperties: DiscordProperties,
) : SlashCommand {

    private val log = KotlinLogging.logger {}

    override val name: String = "정지"

    override fun data(): SlashCommandData = Commands.slash(name, "회원의 서비스 이용을 제한합니다.")
        .addOptions(
            OptionData(OptionType.STRING, "항목", "정지 시킬 항목", true)
                .addChoices(BanTarget.entries.map { Command.Choice(it.label, it.name) }),
            OptionData(OptionType.STRING, "타겟", "정지 시킬 타겟", true),
            OptionData(OptionType.STRING, "사유", "정지 사유", true),
            OptionData(OptionType.INTEGER, "일수", "정지 일수", true)
                .setRequiredRange(1, 365),
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val type = event.getOption("항목")?.asString
        val target = event.getOption("타겟")?.asString
        val reason = event.getOption("사유")?.asString
        val days = event.getOption("일수")?.asInt

        if (type.isNullOrBlank() || target.isNullOrBlank() || reason.isNullOrBlank() || days == null) {
            event.reply("항목, 타겟, 사유, 일수를 모두 입력해주시길 바랍니다.").setEphemeral(true).queue()
            return
        }

        if (days !in 1..365) {
            event.reply("정지 일수는 1일 이상 365일 이하여야 합니다.").setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        val response = try {
            banService.create(
                BanCreateRequest(
                    type = BanType.valueOf(type),
                    target = target,
                    reason = reason,
                    days = days,
                )
            )
        } catch (e: CustomException) {
            event.hook.sendMessage(e.errorCode.message).queue()
            return
        }

        val channel = event.jda.getTextChannelById(discordProperties.banChannelId)
        if (channel == null) {
            log.warn { "정지 채널을 찾을 수 없습니다. 채널 ID = ${discordProperties.banChannelId}" }
            return
        }

        val embed = EmbedBuilder()
            .setTitle("정지")
            .addField("ID", "`${response.banId}`", false)
            .addField("UUID", "`${response.uuid}`", false)
            .addField("항목", response.type.name, false)
            .addField("타겟", "`${response.target}`", false)
            .addField("정지 일수", "${days}일", false)
            .addField("사유", response.reason, false)
            .addField("정지일", "<t:${response.createdAt.epochSecond}:f>", false)
            .addField("해제일", "<t:${response.expiredAt.epochSecond}:f>", false)
            .build()

        channel.sendMessageEmbeds(embed).queue()
        event.hook.sendMessage("정지 처리가 완료되었습니다.").queue()
    }
}