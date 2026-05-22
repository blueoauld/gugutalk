package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.LikeService
import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class LikeController(

    private val likeService: LikeService,
) {

    @GetMapping("/likes/{targetId}", version = "1")
    fun get(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<ActivityStatusResponse> {
        val response = likeService.get(memberId, targetId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/likes/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        likeService.create(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/likes/{targetId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        likeService.delete(memberId, targetId)
        return ResponseEntity.ok().build()
    }
}