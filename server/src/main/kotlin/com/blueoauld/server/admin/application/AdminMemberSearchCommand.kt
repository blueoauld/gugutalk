package com.blueoauld.server.admin.application

import com.blueoauld.server.admin.application.response.AdminGetMemberResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.discord.infrastructure.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class AdminMemberSearchCommand(

    private val adminService: AdminService,
) : SlashCommand {

    override val name: String = "조회"

    override fun data(): SlashCommandData = Commands.slash(name, "닉네임으로 회원을 조회합니다.")
        .addOption(OptionType.STRING, "닉네임", "조회할 닉네임", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val nickname = event.getOption("닉네임")?.asString
        if (nickname.isNullOrBlank()) {
            event.reply("닉네임을 입력해주세요.").setEphemeral(true).queue()
            return
        }

        event.deferReply(true).queue()

        val response = try {
            adminService.getMember(nickname)
        } catch (e: CustomException) {
            event.hook.sendMessage(e.errorCode.message).queue()
            return
        }

        event.hook.sendMessageEmbeds(buildEmbed(response)).queue()
    }

    private fun buildEmbed(response: AdminGetMemberResponse): MessageEmbed {
        val builder = EmbedBuilder()
            .setColor(Color(0x5865F2))
            .setTitle(response.nickname)
            .addField("ID", "`${response.memberId}`", false)
            .addField("성별", response.gender.name, false)
            .addField("지역", response.region.name, false)
            .addField("출생연도", response.birthYear.toString(), false)
            .addField("전화번호", "`${response.phone}`", false)
            .addField("디바이스 ID", "`${response.deviceId}`", false)
            .addField("코멘트", response.comment.ifBlank { "-" }, false)
            .addField("자기소개", response.bio.ifBlank { "-" }, false)
            .addField("가입일", "<t:${response.createdAt.epochSecond}:f>", false)
            .addField("수정일", "<t:${response.updatedAt.epochSecond}:f>", false)

        if (response.publicImages.isNotEmpty()) {
            builder.addField(
                "공개 이미지 (${response.publicImages.size})",
                response.publicImages.joinToString("\n").take(1024), false
            )
        }
        if (response.privateImages.isNotEmpty()) {
            builder.addField(
                "비공개 이미지 (${response.privateImages.size})",
                response.privateImages.joinToString("\n").take(1024), false
            )
        }
        return builder.build()
    }
}