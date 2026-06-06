package com.blueoauld.server.authentication.application.request

import jakarta.validation.constraints.NotBlank

data class RotateTokenRequest(

    val memberId: Long,

    @field:NotBlank(message = "액세스 토큰은 필수입니다.")
    val accessToken: String,

    @field:NotBlank(message = "리프레쉬 토큰은 필수입니다.")
    val refreshToken: String,
)
