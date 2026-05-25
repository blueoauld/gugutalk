package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.result.MemberSearchResult
import java.time.Instant
import java.time.Year

data class MemberSearchRowResponse(

    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val gender: Gender,
    val age: Int,
    val region: Region,
    val updatedAt: Instant,
) {

    companion object {
        fun from(result: MemberSearchResult): MemberSearchRowResponse {
            return MemberSearchRowResponse(
                memberId = result.memberId,
                nickname = result.nickname,
                profileUrl = result.profileUrl,
                gender = result.gender,
                age = Year.now().value - result.birthYear,
                region = result.region,
                updatedAt = result.updatedAt,
            )
        }
    }
}
