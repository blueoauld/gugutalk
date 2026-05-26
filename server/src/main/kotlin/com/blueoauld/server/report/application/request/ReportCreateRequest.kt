package com.blueoauld.server.report.application.request

import com.blueoauld.server.report.entity.type.ReportType
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class ReportCreateRequest(

    val type: ReportType,

    @field:Size(max = 500, message = "신고 사유는 500자 이하여야 합니다.")
    val reason: String,

    @field:Size(
        max = 5,
        message = "이미지는 최대 5개까지 요청할 수 있습니다."
    )
    @field:Valid
    val images: List<ReportImageCreateRequest>
)
