package com.blueoauld.server.authentication.application.request

import com.blueoauld.server.member.entity.type.Gender

data class SignupRequest(

    val phone: String,
    val deviceId: String,
    val verificationCode: String,
    val password: String,
    val confirmPassword: String,
    val gender: Gender,
)
