package com.blueoauld.server.ban.application.response

import com.blueoauld.server.ban.entity.type.BanType
import java.time.Instant

data class BanCreateResponse(

    val banId: Long,
    val type: BanType,
    val target: String,
    val reason: String,
    val createdAt: Instant,
    val expiredAt: Instant,
)
