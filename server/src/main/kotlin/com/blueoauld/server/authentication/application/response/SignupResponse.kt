package com.blueoauld.server.authentication.application.response

data class SignupResponse(

    val accessToken: String,
    val refreshToken: String,
)
