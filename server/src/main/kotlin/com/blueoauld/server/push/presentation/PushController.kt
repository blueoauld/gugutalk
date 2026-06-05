package com.blueoauld.server.push.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.push.application.PushService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class PushController(

    private val pushService: PushService,
) {

    @PostMapping("/push", version = "1")
    fun create(
        @Login memberId: Long,
        @RequestParam(value = "token") token: String,
    ): ResponseEntity<Unit> {
        pushService.upsert(memberId, token)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/push", version = "1")
    fun delete(
        @RequestParam(value = "token") token: String,
    ): ResponseEntity<Unit> {
        pushService.delete(token)
        return ResponseEntity.ok().build()
    }
}