package com.blueoauld.server.authentication.application.request

import com.blueoauld.server.member.entity.type.Gender
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class SignupRequest(

    @field:NotBlank(message = "휴대폰 번호는 필수입니다.")
    @field:Pattern(
        regexp = "^010\\d{8}$",
        message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 01012345678)"
    )
    val phone: String,

    @field:NotBlank(message = "디바이스 ID는 필수입니다.")
    val deviceId: String,

    @field:NotBlank(message = "인증 번호는 필수입니다.")
    @field:Pattern(
        regexp = "^\\d{6}$",
        message = "인증 번호는 6자리 숫자여야 합니다."
    )
    val verificationCode: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,

    @field:NotBlank(message = "비밀번호 확인은 필수입니다.")
    val confirmPassword: String,

    @field:NotNull(message = "성별은 필수입니다.")
    var gender: Gender,
)