package com.blueoauld.server.chat.application.event

import com.blueoauld.server.chat.entity.type.ReactionType

data class ChatMessageReactEvent(

    val chatRoomId: Long,
    val chatMessageId: Long,
    val memberId: Long,
    val type: ReactionType?,
)
