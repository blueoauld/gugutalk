package com.blueoauld.server.authentication.application.request

import com.blueoauld.server.member.entity.type.Region

data class SetupRequest(

    val nickname: String,
    val birthYear: Int,
    val region: Region,
    val bio: String,
)
