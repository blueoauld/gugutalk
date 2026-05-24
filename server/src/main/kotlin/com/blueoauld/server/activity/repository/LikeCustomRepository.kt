package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.repository.result.ActivityResult
import java.time.Instant

fun interface LikeCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ActivityResult>
}