package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.RankResult
import java.time.Instant

interface UnlikeCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult>

    fun findAllByRank(
        gender: String,
        cursorId: Long?,
        cursorScore: Long?,
        size: Int
    ): List<RankResult>
}