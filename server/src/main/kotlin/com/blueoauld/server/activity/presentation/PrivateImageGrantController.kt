package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.PrivateImageGrantService
import com.blueoauld.server.activity.application.response.ActivityStatusResponse
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class PrivateImageGrantController(

    private val privateImageGrantService: PrivateImageGrantService,
) {

    @GetMapping("/private-image-grants/{targetId}", version = "1")
    fun get(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<ActivityStatusResponse> {
        val response = privateImageGrantService.get(memberId, targetId)
        return ResponseEntity.ok(response)
    }

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
}