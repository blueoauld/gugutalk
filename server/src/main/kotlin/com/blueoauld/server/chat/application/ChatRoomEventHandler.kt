package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatRoomDeleteEvent
import com.blueoauld.server.chat.application.event.ChatRoomReadEvent
import com.blueoauld.server.chat.application.event.ChatRoomUpsertEvent
import com.blueoauld.server.chat.application.response.ChatRoomDeleteResponse
import com.blueoauld.server.chat.application.response.ChatRoomReadResponse
import com.blueoauld.server.chat.application.response.ChatRoomUpsertResponse
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatRoomEventHandler(

    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatRoomUpsertEvent) {
        simpMessagingTemplate.convertAndSendToUser(
            event.targetId.toString(),
            "/queue/chat-rooms/upsert",
            ChatRoomUpsertResponse.from(event)
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatRoomDeleteEvent) {
        simpMessagingTemplate.convertAndSendToUser(
            event.targetId.toString(),
            "/queue/chat-rooms/delete",
            ChatRoomDeleteResponse(event.chatRoomId)
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatRoomReadEvent) {
        simpMessagingTemplate.convertAndSendToUser(
            event.memberId.toString(),
            "/queue/chat-rooms/read",
            ChatRoomReadResponse(event.chatRoomId)
        )
    }
}