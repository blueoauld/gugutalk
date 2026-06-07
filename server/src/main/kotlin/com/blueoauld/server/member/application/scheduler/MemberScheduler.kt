package com.blueoauld.server.member.application.scheduler

import com.blueoauld.server.member.application.MemberCleanupService
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.r2.application.R2Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class MemberScheduler(

    private val memberCleanupService: MemberCleanupService,
    private val r2Provider: R2Provider,
    private val memberRepository: MemberRepository,
) {

    private val log = KotlinLogging.logger {}

    companion object {
        private const val BATCH_SIZE = 500
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    fun cleanup() {
        val threshold = Instant.now().minus(30, ChronoUnit.DAYS)
        var total = 0

        while (true) {
            val ids = memberRepository.findDeletedMemberIds(threshold, BATCH_SIZE)
            if (ids.isEmpty()) {
                break
            }

            val imageKeys = memberCleanupService.deleteBatch(ids)
            r2Provider.deleteAll(imageKeys)
            total += ids.size

            if (ids.size < BATCH_SIZE) {
                break
            }
        }
        log.info { "탈퇴한 회원 정보가 삭제되었습니다. ${total}개" }
    }
}