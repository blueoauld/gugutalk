package com.blueoauld.server.authentication.application.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class LoginRequest(

    @field:NotBlank(message = "휴대폰 번호는 필수입니다.")
    @field:Pattern(
        regexp = "^010\\d{8}$",
        message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 01012345678)"
    )
    val phone: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)
