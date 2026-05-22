package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.request.ReviewCreateRequest
import com.blueoauld.server.activity.application.response.ReviewCreateResponse
import com.blueoauld.server.activity.application.response.ReviewRowResponse
import com.blueoauld.server.activity.entity.Review
import com.blueoauld.server.activity.repository.ReviewRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.common.util.RandomNicknameGenerator
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ReviewService(

    private val reviewRepository: ReviewRepository,
) {

    @Transactional
    fun create(memberId: Long, targetId: Long, request: ReviewCreateRequest): ReviewCreateResponse {
        if (memberId == targetId) {
            throw CustomException(ACTIVITY_09)
        }

        val review = Review(
            fromId = memberId,
            toId = targetId,
            nickname = RandomNicknameGenerator.generate(),
            content = request.content
        )
        reviewRepository.save(review)

        return ReviewCreateResponse(
            reviewId = review.id,
            fromId = review.fromId,
            toId = review.toId,
            nickname = review.nickname,
            content = review.content,
            createdAt = review.createdAt,
        )
    }

    @Transactional
    fun delete(memberId: Long, reviewId: Long) {
        val review = reviewRepository.findByIdOrNull(reviewId) ?: throw CustomException(ACTIVITY_10)

        if (review.toId != memberId) {
            throw CustomException(ACTIVITY_11)
        }

        reviewRepository.delete(review)
    }

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ReviewRowResponse> {
        val result = reviewRepository.findAllByCursor(
            memberId = memberId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ReviewRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.reviewId,
            nextDateAt = last?.createdAt,
            hasNext = hasNext
        )
    }
}