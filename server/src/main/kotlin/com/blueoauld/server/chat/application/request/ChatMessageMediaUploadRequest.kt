package com.blueoauld.server.chat.application.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class ChatMessageMediaUploadRequest(

    @field:Size(
        max = 5,
        message = "미디어는 최대 5개까지 요청할 수 있습니다."
    )
    @field:Valid
    val media: List<ChatMessageMediaCreateRequest>
)