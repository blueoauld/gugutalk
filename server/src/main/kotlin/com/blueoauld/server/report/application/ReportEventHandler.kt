package com.blueoauld.server.report.application

import com.blueoauld.server.discord.application.DiscordNotifier
import com.blueoauld.server.r2.application.R2Provider
import com.blueoauld.server.report.application.event.ReportCreateEvent
import com.blueoauld.server.report.application.event.ReportNotifyEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReportEventHandler(

    private val r2Provider: R2Provider,
    private val discordNotifier: DiscordNotifier
) {

    private val log = KotlinLogging.logger {}

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ReportCreateEvent) {
        event.keys.forEach { key ->
            runCatching {
                val fileName = key.substringAfterLast("/")

                r2Provider.moveFile(
                    key,
                    "report/${event.reportId}/$fileName",
                )
            }.onFailure { e ->
                log.error(e) { "신고 이미지 이동에 실패했습니다. 키 = $key" }
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ReportNotifyEvent) {
        discordNotifier.sendReportAlert(event)
    }
}