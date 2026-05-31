package com.blueoauld.server.chat.application.event

data class ChatRoomReadEvent(

    val chatRoomId: Long,
    val memberId: Long,
)
