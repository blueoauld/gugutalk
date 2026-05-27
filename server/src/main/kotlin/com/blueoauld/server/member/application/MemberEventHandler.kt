package com.blueoauld.server.member.application

import com.blueoauld.server.member.application.event.MemberUpdateProfileEvent
import com.blueoauld.server.r2.application.R2Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
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
        runBlocking(Dispatchers.IO) {
            event.moveTasks
                .filter { it.sourceKey != it.destinationKey }
                .map { task ->
                    async {
                        runCatching {
                            r2Provider.moveFile(task.sourceKey, task.destinationKey)
                        }.onFailure { e ->
                            log.error(e) { "프로필 이미지 이동에 실패했습니다. 키 = ${task.sourceKey}" }
                        }
                    }
                }
                .awaitAll()

            event.deleteKeys
                .map { key ->
                    async {
                        runCatching {
                            r2Provider.deleteFile(key)
                        }.onFailure { e ->
                            log.error(e) { "프로필 이미지 삭제에 실패했습니다. 키 = $key" }
                        }
                    }
                }
                .awaitAll()
        }
    }
}