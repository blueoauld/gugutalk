package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.type.Gender
import com.blueoauld.server.member.entity.type.Region
import com.blueoauld.server.member.repository.result.MemberDetailResult
import java.time.Instant
import java.time.Year

data class MemberGetResponse(

    val memberId: Long,
    val images: List<MemberImageResponse>,
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
    val privateImages: Int,
    val isLike: Boolean,
    val isUnlike: Boolean,
    val isPrivateImageGrant: Boolean,
    val hasPrivateImageGrant: Boolean,
    val isBlock: Boolean,
) {

    companion object {
        fun from(result: MemberDetailResult, images: List<MemberImageResponse>, privateImages: Int): MemberGetResponse {
            return MemberGetResponse(
                memberId = result.memberId,
                images = images,
                nickname = result.nickname,
                gender = result.gender,
                age = Year.now().value - result.birthYear,
                region = result.region,
                bio = result.bio,
                isChat = result.isChat,
                updatedAt = result.updatedAt,
                likes = result.likes.toInt(),
                unlikes = result.unlikes.toInt(),
                reviews = result.reviews.toInt(),
                privateImages = privateImages,
                isLike = result.isLike,
                isUnlike = result.isUnlike,
                isPrivateImageGrant = result.isPrivateImageGrant,
                hasPrivateImageGrant = result.hasPrivateImageGrant,
                isBlock = result.isBlock
            )
        }
    }
}
