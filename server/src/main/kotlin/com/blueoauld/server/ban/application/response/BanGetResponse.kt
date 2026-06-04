package com.blueoauld.server.ban.application.response

import com.blueoauld.server.ban.entity.type.BanType
import java.time.Instant

data class BanGetResponse(

    val banId: Long,
    val uuid: String,
    val type: BanType,
    val target: String,
    val reason: String,
    val days: Int,
    val createdAt: Instant,
    val expiredAt: Instant,
)
