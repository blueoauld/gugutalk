package com.blueoauld.server.member.application

import com.blueoauld.server.member.application.event.MemberUpdateProfileEvent
import com.blueoauld.server.r2.application.R2Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class MemberEventHandler(

    private val r2Provider: R2Provider
) {

    private val log = KotlinLogging.logger {}

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: MemberUpdateProfileEvent) {
        event.moveTasks.forEach { task ->
            if (task.sourceKey == task.destinationKey) {
                return@forEach
            }

            runCatching {
                r2Provider.moveFile(task.sourceKey, task.destinationKey)
                log.info { "${task.sourceKey} -> ${task.destinationKey}" }
            }.onFailure { e ->
                log.error(e) { "프로필 이미지 이동에 실패했습니다. 키 = ${task.sourceKey}" }
            }
        }
    }
}