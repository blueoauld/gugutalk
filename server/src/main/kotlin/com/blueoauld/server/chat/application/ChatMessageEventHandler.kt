package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.application.event.ChatMessageUploadMediaEvent
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.r2.application.R2Provider
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatMessageEventHandler(

    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val r2Provider: R2Provider
) {

    private val log = KotlinLogging.logger {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatMessageSendEvent) {
        simpMessagingTemplate.convertAndSend(
            "/topic/chat-rooms/${event.chatRoomId}",
            ChatMessageRowResponse.from(event)
        )
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatMessageUploadMediaEvent) {
        runBlocking(Dispatchers.IO) {
            event.keys.map { key ->
                async {
                    runCatching {
                        val fileName = key.substringAfterLast("/")

                        r2Provider.moveFile(
                            key,
                            "chat/${event.chatRoomId}/$fileName",
                        )
                    }.onFailure { e ->
                        log.error(e) { "채팅 메세지 미디어 이동에 실패했습니다. 키 = $key" }
                    }
                }
            }.awaitAll()
        }
    }
}