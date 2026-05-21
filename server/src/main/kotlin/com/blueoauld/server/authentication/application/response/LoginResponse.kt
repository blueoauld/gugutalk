package com.blueoauld.server.authentication.application.response

data class LoginResponse(

    val accessToken: String,
    val refreshToken: String,
)
