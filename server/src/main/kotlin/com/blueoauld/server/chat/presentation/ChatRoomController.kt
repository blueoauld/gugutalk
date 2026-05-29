package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatRoomService
import com.blueoauld.server.chat.application.request.ChatRoomCreateRequest
import com.blueoauld.server.common.authentication.annotation.Login
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class ChatRoomController(

    private val chatRoomService: ChatRoomService,
) {

    @PostMapping("/chat-rooms/{targetId}", version = "1")
    fun create(
        @Login memberId: Long,
        @PathVariable targetId: Long,
        @Valid @RequestBody request: ChatRoomCreateRequest
    ): ResponseEntity<Unit> {
        chatRoomService.create(memberId, targetId, request)
        return ResponseEntity.ok().build()
    }
}