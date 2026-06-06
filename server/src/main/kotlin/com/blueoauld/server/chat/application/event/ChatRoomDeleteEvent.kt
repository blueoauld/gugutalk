package com.blueoauld.server.chat.application.event

data class ChatRoomDeleteEvent(

    val chatRoomId: Long,
    val targetId: Long,
    val memberId: Long
)
