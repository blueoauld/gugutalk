package com.blueoauld.server.member.application.request

import jakarta.validation.constraints.NotBlank

data class MemberImageCreateRequest(

    @field:NotBlank(message = "URL은 필수입니다.")
    val url: String,

    @field:NotBlank(message = "키 값은 필수입니다.")
    val key: String,
)