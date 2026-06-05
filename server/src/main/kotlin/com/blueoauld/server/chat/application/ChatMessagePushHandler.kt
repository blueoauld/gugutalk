package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessagePushEvent
import com.blueoauld.server.push.application.PushSender
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatMessagePushHandler(

    private val pushSender: PushSender,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ChatMessagePushEvent) {
        pushSender.sendToMember(
            memberId = event.targetId,
            title = event.nickname,
            body = event.lastMessagePreview,
            data = buildMap {
                put("type", "MESSAGE")
                put("chatRoomId", event.chatRoomId.toString())
                put("memberId", event.senderId.toString())
                put("nickname", event.nickname)
                event.profileUrl?.let { put("profileUrl", it) }
            }
        )
    }
}