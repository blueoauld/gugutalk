package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.UnlikeService
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class UnlikeController(

    private val unlikeService: UnlikeService,
) {

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