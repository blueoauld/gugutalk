package com.blueoauld.server.point.repository.result

import com.blueoauld.server.point.entity.type.PointSource
import java.time.Instant

data class PointHistoryResult(

    val pointHistoryId: Long,
    val pointSource: PointSource,
    val createdAt: Instant
)
