package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.ReviewService
import com.blueoauld.server.activity.application.request.ReviewCreateRequest
import com.blueoauld.server.activity.application.response.ReviewCreateResponse
import com.blueoauld.server.common.authentication.annotation.Login
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class ReviewController(

    private val reviewService: ReviewService,
) {

    @PostMapping("/reviews/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long,
        @Valid @RequestBody request: ReviewCreateRequest
    ): ResponseEntity<ReviewCreateResponse> {
        val response = reviewService.create(memberId, targetId, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/reviews/{reviewId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable reviewId: Long
    ): ResponseEntity<Unit> {
        reviewService.delete(memberId, reviewId)
        return ResponseEntity.ok().build()
    }
}