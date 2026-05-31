package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatRoomService
import com.blueoauld.server.chat.application.request.ChatRoomCreateRequest
import com.blueoauld.server.chat.application.response.ChatRoomRowResponse
import com.blueoauld.server.chat.application.response.ChatRoomSearchRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

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

    @DeleteMapping("/chat-rooms/{chatRoomId}", version = "1")
    fun delete(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
    ): ResponseEntity<Unit> {
        chatRoomService.delete(memberId, chatRoomId)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/chat-rooms/{chatRoomId}/read", version = "1")
    fun read(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
    ): ResponseEntity<Unit> {
        chatRoomService.read(memberId, chatRoomId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/chat-rooms", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(defaultValue = "ALL") status: String,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ChatRoomRowResponse>> {
        val response = chatRoomService.gets(memberId, status, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/chat-rooms/search", version = "1")
    fun search(
        @Login memberId: Long,
        @RequestParam nickname: String,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<ChatRoomSearchRowResponse>> {
        val response = chatRoomService.search(memberId, nickname, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}