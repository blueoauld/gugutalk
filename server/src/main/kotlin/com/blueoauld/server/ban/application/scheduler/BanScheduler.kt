package com.blueoauld.server.ban.application.scheduler

import com.blueoauld.server.ban.application.BanCleanupService
import com.blueoauld.server.ban.repository.BanRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class BanScheduler(

    private val banCleanupService: BanCleanupService,
    private val banRepository: BanRepository,
) {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val BATCH_SIZE = 500
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    fun cleanup() {
        val threshold = Instant.now()
        var total = 0

        while (true) {
            val ids = banRepository.findAllByExpiredIds(threshold, BATCH_SIZE)
            if (ids.isEmpty()) {
                break
            }

            banCleanupService.deleteBatch(ids)
            total += ids.size

            if (ids.size < BATCH_SIZE) {
                break
            }
        }
        log.info { "만료된 정지 정보가 삭제되었습니다. ${total}개" }
    }
}