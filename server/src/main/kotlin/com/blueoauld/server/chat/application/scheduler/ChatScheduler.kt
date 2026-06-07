package com.blueoauld.server.chat.application.scheduler

import com.blueoauld.server.chat.application.ChatCleanupService
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.r2.application.R2Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ChatScheduler(

    private val chatCleanupService: ChatCleanupService,
    private val r2Provider: R2Provider,
    private val chatRoomRepository: ChatRoomRepository
) {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val BATCH_SIZE = 500
    }

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    fun cleanup() {
        val threshold = Instant.now().minus(90, ChronoUnit.DAYS)
        var total = 0

        while (true) {
            val ids = chatRoomRepository.findAllByDeletedIds(threshold, BATCH_SIZE)
            if (ids.isEmpty()) {
                break
            }

            val imageKeys = chatCleanupService.deleteBatch(ids)
            r2Provider.deleteAll(imageKeys)
            total += ids.size

            if (ids.size < BATCH_SIZE) {
                break
            }
        }
        log.info { "삭제된 채팅 정보가 삭제되었습니다. ${total}개" }
    }
}