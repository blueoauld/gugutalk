package com.blueoauld.server.common.dto.response

data class CursorScoreResponse<T>(

    val payload: List<T>,
    val nextId: Long?,
    val nextScore: Long?,
    val hasNext: Boolean
)