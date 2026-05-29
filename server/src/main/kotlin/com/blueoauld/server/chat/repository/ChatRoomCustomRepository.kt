package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.repository.result.ChatRoomResult
import java.time.Instant

fun interface ChatRoomCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        status: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatRoomResult>
}