package com.blueoauld.server.chat.repository.result

import com.blueoauld.server.chat.entity.type.MessageType
import java.time.Instant

data class ChatMessageProjection(

    val chatMessageId: Long,
    val senderId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
)
