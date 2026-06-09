package com.blueoauld.server.chat.application.request

import jakarta.validation.constraints.NotBlank

data class ChatMessageSendRequest(

    @field:NotBlank(message = "메세지 내용은 필수입니다.")
    val content: String,

    val clientMessageId: String? = null,
)
