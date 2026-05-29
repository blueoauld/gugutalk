package com.blueoauld.server.chat.application.request

import jakarta.validation.constraints.NotBlank

data class ChatRoomCreateRequest(

    @field:NotBlank(message = "쪽지 내용은 필수입니다.")
    val content: String,
)
