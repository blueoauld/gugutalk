package com.blueoauld.server.member.repository.result

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

data class MemberDetailResult(

    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val gender: Gender,
    val birthYear: Int,
    val region: Region,
    val bio: String,
    val isChat: Boolean,
    val updatedAt: Instant,
    val likes: Long,
    val unlikes: Long,
    val reviews: Long,
    val likeFromMe: Long,
    val unlikeFromMe: Long,
    val blockFromMe: Long,
    val privateGrantFromMe: Long,
    val privateGrantToMe: Long,
) {

    val isLike: Boolean get() = likeFromMe > 0
    val isUnlike: Boolean get() = unlikeFromMe > 0
    val isBlock: Boolean get() = blockFromMe > 0
    val isPrivateImageGrant: Boolean get() = privateGrantFromMe > 0
    val hasPrivateImageGrant: Boolean get() = privateGrantToMe > 0
}