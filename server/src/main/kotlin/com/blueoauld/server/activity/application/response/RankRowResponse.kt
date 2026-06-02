package com.blueoauld.server.activity.application.response

import com.blueoauld.server.activity.repository.result.RankResult
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant
import java.time.Year

data class RankRowResponse(

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
        fun from(result: RankResult): RankRowResponse {
            return RankRowResponse(
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
