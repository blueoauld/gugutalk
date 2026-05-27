package com.blueoauld.server.member.application.event

import com.blueoauld.server.member.application.response.MemberImageMoveTask

data class MemberUpdateProfileEvent(

    val memberId: Long,
    val moveTasks: List<MemberImageMoveTask>,
    val deleteKeys: List<String>,
)
