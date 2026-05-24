package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.LikeService
import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RequestMapping("/api")
@RestController
class LikeController(

    private val likeService: LikeService,
) {

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

    @GetMapping("/likes", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ActivityRowResponse>> {
        val response = likeService.gets(memberId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}