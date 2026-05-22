package com.blueoauld.server.common.dto.response

import java.time.Instant

data class CursorResponse<T>(

    val payload: List<T>,
    val nextId: Long?,
    val nextDateAt: Instant?,
    val hasNext: Boolean
)