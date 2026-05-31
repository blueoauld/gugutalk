package com.blueoauld.server.chat.application.event

import java.time.Instant

data class ChatRoomSendEvent(

    val chatRoomId: Long,
    val memberId: Long,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
)
