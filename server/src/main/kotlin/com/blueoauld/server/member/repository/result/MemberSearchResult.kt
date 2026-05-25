package com.blueoauld.server.member.repository.result

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

data class MemberSearchResult(

    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val gender: Gender,
    val birthYear: Int,
    val region: Region,
    val updatedAt: Instant,
)
