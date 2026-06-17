package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.entity.type.ReactionType
import com.blueoauld.server.chat.repository.result.ChatMessageReactionResult

data class ChatMessageReactionResponse(

    val memberId: Long,
    val type: ReactionType,
) {

    companion object {
        fun from(result: ChatMessageReactionResult): ChatMessageReactionResponse {
            return ChatMessageReactionResponse(
                memberId = result.memberId,
                type = result.type,
            )
        }
    }
}
