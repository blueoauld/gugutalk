package com.blueoauld.server.member.application.request

import jakarta.validation.constraints.NotBlank

data class UpdateCommentRequest(

    @field:NotBlank(message = "코멘트는 필수입니다.")
    val content: String
)
