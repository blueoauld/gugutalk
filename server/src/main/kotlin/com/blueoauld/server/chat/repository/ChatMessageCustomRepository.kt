package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.repository.result.ChatMessageResult
import java.time.Instant

fun interface ChatMessageCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        chatRoomId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatMessageResult>
}