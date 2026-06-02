package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.activity.repository.result.RankResult
import java.time.Instant

interface LikeCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult>

    fun findAllByRank(
        cursorId: Long?,
        cursorScore: Long?,
        size: Int
    ): List<RankResult>
}