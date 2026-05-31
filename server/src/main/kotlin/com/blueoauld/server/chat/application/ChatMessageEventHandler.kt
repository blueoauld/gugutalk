package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.application.event.ChatRoomSendEvent
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.chat.application.response.ChatRoomRowResponse
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatRoomSendEvent) {
        simpMessagingTemplate.convertAndSendToUser(
            event.memberId.toString(),
            "/queue/chat-rooms",
            ChatRoomRowResponse.from(event)
        )
    }
}