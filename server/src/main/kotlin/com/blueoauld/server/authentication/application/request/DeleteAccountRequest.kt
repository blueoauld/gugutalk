package com.blueoauld.server.authentication.application.request

import jakarta.validation.constraints.NotBlank

data class DeleteAccountRequest(

    @field:NotBlank(message = "리프레쉬 토큰은 필수입니다.")
    val refreshToken: String,
)
