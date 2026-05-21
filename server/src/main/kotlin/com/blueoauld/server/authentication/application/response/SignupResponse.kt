package com.blueoauld.server.authentication.application.response

data class SignupResponse(

    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
)
