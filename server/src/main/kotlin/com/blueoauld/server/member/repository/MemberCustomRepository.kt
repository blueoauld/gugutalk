package com.blueoauld.server.member.repository

import com.blueoauld.server.member.repository.result.MemberResult
import java.time.Instant

fun interface MemberCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        gender: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<MemberResult>
}