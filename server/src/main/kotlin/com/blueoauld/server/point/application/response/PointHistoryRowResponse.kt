package com.blueoauld.server.point.application.response

import com.blueoauld.server.point.entity.type.PointType
import com.blueoauld.server.point.repository.result.PointHistoryResult
import java.time.Instant

data class PointHistoryRowResponse(

    val pointHistoryId: Long,
    val description: String,
    val type: PointType,
    val point: Long,
    val createdAt: Instant
) {

    companion object {
        fun from(result: PointHistoryResult): PointHistoryRowResponse {
            return PointHistoryRowResponse(
                pointHistoryId = result.pointHistoryId,
                description = result.pointSource.description,
                type = result.pointSource.type,
                point = result.pointSource.point,
                createdAt = result.createdAt
            )
        }
    }
}
