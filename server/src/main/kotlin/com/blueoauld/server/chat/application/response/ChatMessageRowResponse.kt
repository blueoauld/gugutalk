package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.result.ChatMessageResult
import java.time.Instant

data class ChatMessageRowResponse(

    val chatMessageId: Long,
    val senderId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
) {

    companion object {
        fun from(result: ChatMessageResult): ChatMessageRowResponse {
            return ChatMessageRowResponse(
                chatMessageId = result.chatMessageId,
                senderId = result.senderId,
                content = result.content,
                type = result.type,
                createdAt = result.createdAt
            )
        }
    }
}
