package com.blueoauld.server.activity.application.response

import java.time.Instant

data class ReviewCreateResponse(

    val reviewId: Long,
    val fromId: Long,
    val toId: Long,
    val nickname: String,
    val content: String,
    val createdAt: Instant
)