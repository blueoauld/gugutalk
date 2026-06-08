package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType

data class MemberImageResponse(

    val imageId: Long,
    val type: MemberImageType,
    val key: String,
    val url: String,
    val sortOrder: Int,
) {

    companion object {
        fun from(memberImage: MemberImage): MemberImageResponse {
            return MemberImageResponse(
                imageId = memberImage.id,
                type = memberImage.type,
                key = memberImage.key,
                url = memberImage.url,
                sortOrder = memberImage.sortOrder
            )
        }

        fun from(memberImage: MemberImage, url: String): MemberImageResponse {
            return MemberImageResponse(
                imageId = memberImage.id,
                type = memberImage.type,
                key = memberImage.key,
                url = url,
                sortOrder = memberImage.sortOrder
            )
        }
    }
}
