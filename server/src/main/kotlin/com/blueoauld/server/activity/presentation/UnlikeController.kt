package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.UnlikeService
import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class UnlikeController(

    private val unlikeService: UnlikeService,
) {

    @GetMapping("/unlikes/{targetId}", version = "1")
    fun get(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<ActivityStatusResponse> {
        val response = unlikeService.get(memberId, targetId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/unlikes/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        unlikeService.create(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/unlikes/{targetId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        unlikeService.delete(memberId, targetId)
        return ResponseEntity.ok().build()
    }
}