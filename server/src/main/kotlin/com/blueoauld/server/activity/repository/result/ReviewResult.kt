package com.blueoauld.server.activity.repository.result

import java.time.Instant

data class ReviewResult(

    val reviewId: Long,
    val fromId: Long,
    val toId: Long,
    val nickname: String,
    val content: String,
    val createdAt: Instant
)
