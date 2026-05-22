package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

data class MemberGetResponse(

    val memberId: Long,
    val nickname: String,
    val gender: Gender,
    val age: Int,
    val region: Region,
    val bio: String,
    val isChat: Boolean,
    val updatedAt: Instant,
    val likes: Int,
    val unlikes: Int,
    val reviews: Int,
    val isLike: Boolean,
    val isUnlike: Boolean,
    val isPrivateImageGrant: Boolean,
    val hasPrivateImageGrant: Boolean,
    val isBlock: Boolean,
)
