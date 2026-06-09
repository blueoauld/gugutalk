package com.blueoauld.server.chat.application.event

import com.blueoauld.server.chat.entity.type.MessageType
import java.time.Instant

data class ChatMessageSendEvent(

    val chatMessageId: Long,
    val chatRoomId: Long,
    val senderId: Long,
    val content: String,
    val type: MessageType,
    val createdAt: Instant,
    val clientMessageId: String? = null,
)
