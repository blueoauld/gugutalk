package com.blueoauld.server.authentication.application.response

data class RotateTokenResponse(

    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
)
