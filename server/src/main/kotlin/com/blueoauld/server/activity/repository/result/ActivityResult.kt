package com.blueoauld.server.activity.repository.result

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

data class ActivityResult(

    val activityId: Long,
    val toId: Long,
    val profileUrl: String?,
    val nickname: String,
    val gender: Gender,
    val birthYear: Int,
    val region: Region,
    val createdAt: Instant
)
