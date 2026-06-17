package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.application.event.ChatMessageReactEvent
import com.blueoauld.server.chat.entity.type.ReactionType
import com.blueoauld.server.chat.repository.result.ChatMessageReactionResult

data class ChatMessageReactResponse(

    val chatMessageId: Long,
    val memberId: Long,
    val type: ReactionType? = null,
) {

    companion object {
        fun from(result: ChatMessageReactionResult): ChatMessageReactResponse {
            return ChatMessageReactResponse(
                chatMessageId = result.chatMessageId,
                memberId = result.memberId,
                type = result.type,
            )
        }

        fun from(event: ChatMessageReactEvent): ChatMessageReactResponse {
            return ChatMessageReactResponse(
                chatMessageId = event.chatMessageId,
                memberId = event.memberId,
                type = event.type
            )
        }
    }
}
