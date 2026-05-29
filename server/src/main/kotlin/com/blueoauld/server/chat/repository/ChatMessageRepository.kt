package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    @Query(
        value = "SELECT max(cm.id) FROM ChatMessage cm WHERE cm.chatRoomId = :chatRoomId"
    )
    fun findLastMessageId(chatRoomId: Long): Long?
}