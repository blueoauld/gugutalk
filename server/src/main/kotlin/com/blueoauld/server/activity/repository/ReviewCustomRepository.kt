package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.repository.result.ReviewResult
import java.time.Instant

fun interface ReviewCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ReviewResult>
}