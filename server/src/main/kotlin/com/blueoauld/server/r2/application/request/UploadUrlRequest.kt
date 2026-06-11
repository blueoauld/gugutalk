package com.blueoauld.server.r2.application.request

import jakarta.validation.constraints.NotBlank

data class UploadUrlRequest(

    @field:NotBlank(message = "컨텐츠 타입은 필수입니다.")
    val contentType: String,

    val contentLength: Long,
)
