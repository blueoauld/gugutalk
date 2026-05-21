package com.blueoauld.server.authentication.application.request

data class LoginRequest(

    val phone: String,
    val password: String,
)
