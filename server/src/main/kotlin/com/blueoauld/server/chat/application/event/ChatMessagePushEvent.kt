package com.blueoauld.server.chat.application.event

data class ChatMessagePushEvent(

    val chatRoomId: Long,
    val targetId: Long,
    val senderId: Long,
    val nickname: String,
    val profileUrl: String?,
    val lastMessagePreview: String,
)
