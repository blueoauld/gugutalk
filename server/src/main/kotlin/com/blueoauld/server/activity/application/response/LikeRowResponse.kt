package com.blueoauld.server.activity.application.response

import com.blueoauld.server.activity.repository.result.ActivityResult
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant
import java.time.Year

data class LikeRowResponse(

    val likeId: Long,
    val toId: Long,
    val profileUrl: String?,
    val nickname: String,
    val gender: Gender,
    val age: Int,
    val region: Region,
    val createdAt: Instant
) {

    companion object {
        fun from(result: ActivityResult): LikeRowResponse {
            return LikeRowResponse(
                likeId = result.activityId,
                toId = result.toId,
                profileUrl = result.profileUrl,
                nickname = result.nickname,
                gender = result.gender,
                age = Year.now().value - result.birthYear,
                region = result.region,
                createdAt = result.createdAt,
            )
        }
    }
}