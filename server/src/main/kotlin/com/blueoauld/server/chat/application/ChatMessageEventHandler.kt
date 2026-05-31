package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatMessageEventHandler(

    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatMessageSendEvent) {
        simpMessagingTemplate.convertAndSend(
            "/topic/chat-rooms/${event.chatRoomId}",
            ChatMessageRowResponse.from(event)
        )
    }
}