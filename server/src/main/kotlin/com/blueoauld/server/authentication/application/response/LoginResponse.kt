package com.blueoauld.server.authentication.application.response

data class LoginResponse(

    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
)
