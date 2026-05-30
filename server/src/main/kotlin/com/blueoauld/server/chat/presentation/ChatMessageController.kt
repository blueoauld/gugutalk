package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatMessageService
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
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
}