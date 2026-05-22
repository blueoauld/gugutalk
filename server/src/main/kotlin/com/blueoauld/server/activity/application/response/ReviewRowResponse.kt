package com.blueoauld.server.activity.application.response

import com.blueoauld.server.activity.repository.result.ReviewResult
import java.time.Instant

data class ReviewRowResponse(

    val reviewId: Long,
    val fromId: Long,
    val toId: Long,
    val nickname: String,
    val content: String,
    val createdAt: Instant
) {

    companion object {
        fun from(result: ReviewResult): ReviewRowResponse {
            return ReviewRowResponse(
                reviewId = result.reviewId,
                fromId = result.fromId,
                toId = result.toId,
                nickname = result.nickname,
                content = result.content,
                createdAt = result.createdAt
            )
        }
    }
}