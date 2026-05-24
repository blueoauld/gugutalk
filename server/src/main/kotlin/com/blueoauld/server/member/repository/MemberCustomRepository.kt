package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.result.MemberResult
import java.time.Instant

interface MemberCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        gender: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<MemberResult>

    fun findAllByRegion(
        memberId: Long,
        gender: String,
        region: Region,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<MemberResult>
}