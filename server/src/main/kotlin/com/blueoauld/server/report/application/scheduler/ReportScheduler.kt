package com.blueoauld.server.report.application.scheduler

import com.blueoauld.server.r2.application.R2Provider
import com.blueoauld.server.report.application.ReportCleanupService
import com.blueoauld.server.report.repository.ReportRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ReportScheduler(

    private val reportCleanupService: ReportCleanupService,
    private val r2Provider: R2Provider,
    private val reportRepository: ReportRepository
) {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val BATCH_SIZE = 500
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    fun cleanup() {
        val threshold = Instant.now().minus(365, ChronoUnit.DAYS)
        var total = 0

        while (true) {
            val ids = reportRepository.findExpiredReportIds(threshold, BATCH_SIZE)
            if (ids.isEmpty()) {
                break
            }

            val imageKeys = reportCleanupService.deleteBatch(ids)
            r2Provider.deleteAll(imageKeys)
            total += ids.size

            if (ids.size < BATCH_SIZE) {
                break
            }
        }
        log.info { "기한이 만료된 신고 정보가 삭제되었습니다. ${total}개" }
    }
}