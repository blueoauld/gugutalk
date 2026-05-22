package com.blueoauld.server.activity.application.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReviewCreateRequest(

    @field:NotBlank(message = "내용은 필수입니다.")
    @field:Size(max = 500, message = "내용은 500자 이하여야 합니다.")
    val content: String,
)
