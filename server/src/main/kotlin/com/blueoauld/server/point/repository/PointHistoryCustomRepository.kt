package com.blueoauld.server.point.repository

import com.blueoauld.server.point.repository.result.PointHistoryResult
import java.time.Instant

fun interface PointHistoryCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<PointHistoryResult>
}