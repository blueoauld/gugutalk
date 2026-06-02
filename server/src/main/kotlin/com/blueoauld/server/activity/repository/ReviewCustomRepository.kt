package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.repository.result.RankResult
import com.blueoauld.server.activity.repository.result.ReviewResult
import java.time.Instant

interface ReviewCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ReviewResult>

    fun findAllByRank(
        cursorId: Long?,
        cursorScore: Long?,
        size: Int
    ): List<RankResult>
}