package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.repository.result.ChatRoomResult
import com.blueoauld.server.chat.repository.result.ChatRoomSearchResult
import java.time.Instant

interface ChatRoomCustomRepository {

    fun findAllByCursor(
        memberId: Long,
        status: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatRoomResult>

    fun findAllByNickname(
        memberId: Long,
        nickname: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): List<ChatRoomSearchResult>
}