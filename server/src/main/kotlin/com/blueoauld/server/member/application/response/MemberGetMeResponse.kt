package com.blueoauld.server.member.application.response

import com.blueoauld.server.member.entity.type.Region

data class MemberGetMeResponse(

    val memberId: Long,
    val images: List<MemberImageResponse>,
    val nickname: String,
    val birthYear: Int,
    val region: Region,
    val bio: String,
)
