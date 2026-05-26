package com.blueoauld.server.r2.application.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class UploadUrlRequests(

    @field:Size(
        min = 1,
        max = 5,
        message = "URL은 최소 1개, 최대 5개까지 요청할 수 있습니다."
    )
    @field:Valid
    val urls: List<UploadUrlRequest>,
)
