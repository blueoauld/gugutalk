package com.blueoauld.server.member.application.response

data class MemberGetPrivateImagesResponse(

    val phone: String,
    val images: List<MemberImageResponse>,
)
