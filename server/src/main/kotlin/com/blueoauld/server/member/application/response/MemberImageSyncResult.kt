package com.blueoauld.server.member.application.response

data class MemberImageSyncResult(

    val moveTasks: List<MemberImageMoveTask>,
    val deleteKeys: List<String>,
    val firstImageUrl: String?,
)
