package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatMessageService
import com.blueoauld.server.chat.application.request.ChatMessageMediaUploadRequest
import com.blueoauld.server.chat.application.request.ChatMessageSendRequest
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RequestMapping("/api")
@RestController
class ChatMessageController(

    private val chatMessageService: ChatMessageService,
) {

    @GetMapping("/chat-rooms/{chatRoomId}/messages", version = "1")
    fun gets(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "50") size: Int,
    ): ResponseEntity<CursorResponse<ChatMessageRowResponse>> {
        val response = chatMessageService.gets(memberId, chatRoomId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/chat-rooms/{chatRoomId}/messages", version = "1")
    fun send(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
        @Valid @RequestBody request: ChatMessageSendRequest
    ): ResponseEntity<Unit> {
        chatMessageService.send(memberId, chatRoomId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/chat-rooms/{chatRoomId}/media", version = "1")
    fun uploadMedia(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
        @Valid @RequestBody request: ChatMessageMediaUploadRequest
    ): ResponseEntity<Unit> {
        chatMessageService.uploadMedia(memberId, chatRoomId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/chat-rooms/{chatRoomId}/urls", version = "1")
    fun createUploadUrls(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
        @Valid @RequestBody requests: UploadUrlRequests
    ): ResponseEntity<UploadUrlResponses> {
        val responses = chatMessageService.createUploadUrls(memberId, chatRoomId, requests)
        return ResponseEntity.ok(responses)
    }
}