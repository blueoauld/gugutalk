package com.blueoauld.server.admin.application.response

import com.blueoauld.server.member.entity.Member
import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import java.time.Instant

data class AdminGetMemberResponse(

    val memberId: Long,
    val phone: String,
    val deviceId: String,
    val nickname: String,
    val gender: Gender,
    val region: Region,
    val birthYear: Int,
    val comment: String,
    val bio: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val publicImages: List<String>,
    val privateImages: List<String>,
) {

    companion object {
        fun from(member: Member, publicImages: List<String>, privateImages: List<String>): AdminGetMemberResponse {
            return AdminGetMemberResponse(
                memberId = member.id,
                phone = member.phone,
                deviceId = member.deviceId,
                nickname = member.nickname,
                gender = member.gender,
                region = member.region,
                birthYear = member.birthYear,
                comment = member.comment,
                bio = member.bio,
                createdAt = member.createdAt,
                updatedAt = member.updatedAt,
                publicImages = publicImages,
                privateImages = privateImages
            )
        }
    }
}
