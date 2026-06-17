package com.blueoauld.server.chat.repository.result

import com.blueoauld.server.chat.entity.type.ReactionType

data class ChatMessageReactionResult(

    val chatMessageId: Long,
    val memberId: Long,
    val type: ReactionType
)
