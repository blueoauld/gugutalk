package com.blueoauld.server.member.application.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberUpdateCommentRequest(

    @field:NotBlank(message = "코멘트는 필수입니다.")
    @field:Size(max = 50, message = "코멘트는 50자 이하여야 합니다.")
    val content: String
)
