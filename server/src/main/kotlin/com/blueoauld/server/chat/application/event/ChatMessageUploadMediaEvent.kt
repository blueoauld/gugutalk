package com.blueoauld.server.chat.application.event

data class ChatMessageUploadMediaEvent(

    val chatRoomId: Long,
    val keys: List<String>,
)
