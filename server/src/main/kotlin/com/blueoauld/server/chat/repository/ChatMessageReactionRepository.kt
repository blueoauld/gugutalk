package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatMessageReaction
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageReactionRepository : JpaRepository<ChatMessageReaction, Long> {

    fun findByChatMessageIdAndMemberId(chatMessageId: Long, memberId: Long): ChatMessageReaction?
}