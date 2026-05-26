package com.blueoauld.server.report.application.request

import jakarta.validation.constraints.NotBlank

data class ReportImageCreateRequest(

    @field:NotBlank(message = "URL은 필수입니다.")
    val url: String,

    @field:NotBlank(message = "키 값은 필수입니다.")
    val key: String,
)