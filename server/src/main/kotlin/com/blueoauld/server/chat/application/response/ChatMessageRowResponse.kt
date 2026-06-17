package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.result.ChatMessageResult
import java.time.Instant

data class ChatMessageRowResponse(

    val chatMessageId: Long,
    val clientMessageId: String? = null,
    val senderId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
    val reactions: List<ChatMessageReactionResponse> = emptyList()
) {

    companion object {
        fun from(result: ChatMessageResult): ChatMessageRowResponse {
            return ChatMessageRowResponse(
                chatMessageId = result.chatMessageId,
                senderId = result.senderId,
                content = result.content,
                type = result.type,
                createdAt = result.createdAt,
                reactions = result.reactions.map { ChatMessageReactionResponse.from(it) },
            )
        }

        fun from(event: ChatMessageSendEvent): ChatMessageRowResponse {
            return ChatMessageRowResponse(
                chatMessageId = event.chatMessageId,
                clientMessageId = event.clientMessageId,
                senderId = event.senderId,
                content = event.content,
                type = event.type,
                createdAt = event.createdAt
            )
        }
    }
}
