package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.application.event.ChatRoomSendEvent
import com.blueoauld.server.chat.repository.result.ChatRoomResult
import java.time.Instant

data class ChatRoomRowResponse(

    val chatRoomId: Long,
    val memberId: Long,
    val nickname: String,
    val profileUrl: String?,
    val unreadCount: Long,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
) {

    companion object {
        fun from(result: ChatRoomResult): ChatRoomRowResponse {
            return ChatRoomRowResponse(
                chatRoomId = result.chatRoomId,
                memberId = result.memberId,
                nickname = result.nickname,
                profileUrl = result.profileUrl,
                unreadCount = result.unreadCount,
                lastMessagePreview = result.lastMessagePreview,
                lastMessageAt = result.lastMessageAt
            )
        }

        fun from(event: ChatRoomSendEvent): ChatRoomRowResponse {
            return ChatRoomRowResponse(
                chatRoomId = event.chatRoomId,
                memberId = event.memberId,
                nickname = event.nickname,
                profileUrl = event.profileUrl,
                unreadCount = 0,
                lastMessagePreview = event.lastMessagePreview,
                lastMessageAt = event.lastMessageAt
            )
        }
    }
}
