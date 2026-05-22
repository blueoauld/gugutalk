package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.BlockService
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
}