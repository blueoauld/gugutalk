package com.blueoauld.server.authentication.application.request

data class SendVerificationCodeRequest(

    val phone: String,
    val deviceId: String,
)
