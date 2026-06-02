package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.UnlikeService
import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.activity.application.response.RankRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.dto.response.CursorScoreResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

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

    @GetMapping("/unlikes", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ActivityRowResponse>> {
        val response = unlikeService.gets(memberId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/unlikes/rank", version = "1")
    fun getsByRank(
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorScore: Long?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorScoreResponse<RankRowResponse>> {
        val response = unlikeService.getsByRank(cursorId, cursorScore, size)
        return ResponseEntity.ok(response)
    }
}