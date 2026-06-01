package com.blueoauld.server.chat.application.request

import com.blueoauld.server.chat.entity.type.MessageType
import jakarta.validation.constraints.NotBlank

data class ChatMessageMediaCreateRequest(

    val type: MessageType,

    @field:NotBlank(message = "URL은 필수입니다.")
    val url: String,

    @field:NotBlank(message = "키 값은 필수입니다.")
    val key: String,

    val thumbnailUrl: String?,
    val thumbnailKey: String?,
)