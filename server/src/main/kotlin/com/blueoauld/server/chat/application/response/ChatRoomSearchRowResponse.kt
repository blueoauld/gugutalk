package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.repository.result.ChatRoomSearchResult
import java.time.Instant

data class ChatRoomSearchRowResponse(

    val chatRoomId: Long,
    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
) {

    companion object {
        fun from(result: ChatRoomSearchResult): ChatRoomSearchRowResponse {
            return ChatRoomSearchRowResponse(
                chatRoomId = result.chatRoomId,
                memberId = result.memberId,
                nickname = result.nickname,
                profileUrl = result.profileUrl,
                lastMessagePreview = result.lastMessagePreview,
                lastMessageAt = result.lastMessageAt
            )
        }
    }
}
