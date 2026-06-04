package com.blueoauld.server.ban.application.request

import com.blueoauld.server.ban.entity.type.BanType
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class BanCreateRequest(

    @field:NotBlank(message = "정지 유형은 필수입니다.")
    val type: BanType,

    @field:NotBlank(message = "정지 타겟은 필수입니다.")
    val target: String,

    @field:NotBlank(message = "정지 내용은 필수입니다.")
    val reason: String,

    @field:Range(min = 1, max = 365, "정지 일수는 1일 이상 365일 이하여야 합니다.")
    val days: Int,
)
