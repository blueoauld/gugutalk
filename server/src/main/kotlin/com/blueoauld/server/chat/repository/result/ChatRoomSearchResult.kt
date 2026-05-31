package com.blueoauld.server.chat.repository.result

import java.time.Instant

data class ChatRoomSearchResult(

    val chatRoomId: Long,
    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
)
