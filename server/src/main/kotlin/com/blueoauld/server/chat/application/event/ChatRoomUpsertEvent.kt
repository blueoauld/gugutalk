package com.blueoauld.server.chat.application.event

import java.time.Instant

data class ChatRoomUpsertEvent(

    val chatRoomId: Long,
    val targetId: Long,
    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
)
