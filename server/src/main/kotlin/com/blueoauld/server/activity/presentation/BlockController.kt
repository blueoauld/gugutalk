package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.BlockService
import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RequestMapping("/api")
@RestController
class BlockController(

    private val blockService: BlockService,
) {

    @PostMapping("/blocks/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        blockService.create(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/blocks/{targetId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        blockService.delete(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/blocks", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ActivityRowResponse>> {
        val response = blockService.gets(memberId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}