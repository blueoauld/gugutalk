package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatMessageReactionService
import com.blueoauld.server.chat.entity.type.ReactionType
import com.blueoauld.server.common.authentication.annotation.Login
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class ChatMessageReactionController(

    private val chatMessageReactionService: ChatMessageReactionService
) {

    @PutMapping("/chat-rooms/{chatRoomId}/messages/{chatMessageId}/reactions")
    fun react(
        @Login memberId: Long,
        @PathVariable chatRoomId: Long,
        @PathVariable chatMessageId: Long,
        @RequestParam(value = "type") type: ReactionType,
    ): ResponseEntity<Unit> {
        chatMessageReactionService.react(memberId, chatRoomId, chatMessageId, type)
        return ResponseEntity.ok().build()
    }
}