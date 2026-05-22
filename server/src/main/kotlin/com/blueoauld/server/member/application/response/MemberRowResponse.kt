package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.result.MemberResult
import java.time.Instant
import java.time.Year

data class MemberRowResponse(

    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val gender: Gender,
    val age: Int,
    val region: Region,
    val comment: String,
    val updatedAt: Instant,

    val likes: Long,
    val unlikes: Long,
    val reviews: Long,
) {

    companion object {
        fun from(result: MemberResult): MemberRowResponse {
            return MemberRowResponse(
                memberId = result.memberId,
                nickname = result.nickname,
                profileUrl = result.profileUrl,
                gender = result.gender,
                age = Year.now().value - result.birthYear,
                region = result.region,
                comment = result.comment,
                updatedAt = result.updatedAt,
                likes = result.likes,
                unlikes = result.unlikes,
                reviews = result.reviews
            )
        }
    }
}
