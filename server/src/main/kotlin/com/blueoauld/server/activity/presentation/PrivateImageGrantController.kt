package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.PrivateImageGrantService
import com.blueoauld.server.activity.application.response.ActivityRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RequestMapping("/api")
@RestController
class PrivateImageGrantController(

    private val privateImageGrantService: PrivateImageGrantService,
) {

    @PostMapping("/private-image-grants/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        privateImageGrantService.create(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/private-image-grants/{targetId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<Unit> {
        privateImageGrantService.delete(memberId, targetId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/private-image-grants", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ActivityRowResponse>> {
        val response = privateImageGrantService.gets(memberId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}