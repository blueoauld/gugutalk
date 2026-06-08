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

    companion object {
        private val EMBED_COLOR = Color(0x5865F2)
        private const val MAX_DESCRIPTION_LENGTH = 4096
    }

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

        event.hook.sendMessageEmbeds(buildEmbeds(response)).queue()
    }

    private fun buildEmbeds(response: AdminGetMemberResponse): List<MessageEmbed> {
        val embeds = mutableListOf<MessageEmbed>()

        embeds += buildInfoEmbed(response)

        if (response.publicImages.isNotEmpty()) {
            embeds += buildImageEmbeds("공개 이미지", response.publicImages)
        }
        if (response.privateImages.isNotEmpty()) {
            embeds += buildImageEmbeds("비공개 이미지", response.privateImages)
        }

        return embeds
    }

    private fun buildInfoEmbed(response: AdminGetMemberResponse): MessageEmbed =
        EmbedBuilder()
            .setColor(EMBED_COLOR)
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
            .build()

    private fun buildImageEmbeds(title: String, images: List<String>): List<MessageEmbed> {
        val chunks = chunkByLength(images, MAX_DESCRIPTION_LENGTH)

        return chunks.mapIndexed { index, chunk ->
            val header = if (chunks.size == 1) {
                "$title (${images.size})"
            } else {
                "$title (${images.size}) - ${index + 1}/${chunks.size}"
            }
            EmbedBuilder()
                .setColor(EMBED_COLOR)
                .setTitle(header)
                .setDescription(chunk.joinToString("\n"))
                .build()
        }
    }

    private fun chunkByLength(items: List<String>, maxLength: Int): List<List<String>> {
        val chunks = mutableListOf<List<String>>()
        var current = mutableListOf<String>()
        var currentLength = 0

        for (item in items) {
            val extra = if (current.isEmpty()) item.length else item.length + 1

            if (current.isNotEmpty() && currentLength + extra > maxLength) {
                chunks += current
                current = mutableListOf()
                currentLength = 0
            }
            currentLength += if (current.isEmpty()) item.length else item.length + 1
            current += item
        }
        if (current.isNotEmpty()) {
            chunks += current
        }
        return chunks
    }
}