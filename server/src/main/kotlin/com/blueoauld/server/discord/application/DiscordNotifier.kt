package com.blueoauld.server.discord.application

import com.blueoauld.server.common.properties.DiscordProperties
import com.blueoauld.server.report.application.event.ReportNotifyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class DiscordNotifier(

    private val jda: JDA,
    private val discordProperties: DiscordProperties,
) {

    private val log = KotlinLogging.logger {}

    fun sendReportAlert(event: ReportNotifyEvent) {
        val channel = jda.getTextChannelById(discordProperties.reportChannelId)
        if (channel == null) {
            log.warn { "신고 알림 채널을 찾을 수 없습니다. 채널 ID = ${discordProperties.reportChannelId}" }
            return
        }

        val builder = EmbedBuilder()
            .setColor(Color(0xED4245))
            .setTitle(event.type.name)
            .addField("ID", event.reportId.toString(), false)
            .addField("신고자 ID", "`${event.fromMemberId}`", false)
            .addField("신고자 닉네임", "`${event.fromNickname}`", false)
            .addField("신고자 휴대폰", "`${event.fromPhone}`", false)
            .addField("피신고자 ID", "`${event.toMemberId}`", false)
            .addField("피신고자 닉네임", "`${event.toNickname}`", false)
            .addField("피신고자 휴대폰", "`${event.toPhone}`", false)
            .addField("사유", event.reason.ifBlank { "-" }, false)
            .addField("접수일", "<t:${event.createdAt.epochSecond}:f>", false)

        if (event.images.isNotEmpty()) {
            builder.addField(
                "이미지 (${event.images.size})",
                event.images.joinToString("\n").take(1024), false
            )
        }

        channel.sendMessageEmbeds(builder.build()).queue(null) { e ->
            log.error(e) { "신고 알림 전송에 실패했습니다. 신고 ID = ${event.reportId}" }
        }
    }
}