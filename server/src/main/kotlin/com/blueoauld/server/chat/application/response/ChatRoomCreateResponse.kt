package com.blueoauld.server.chat.application.response

import com.blueoauld.server.chat.application.event.ChatRoomUpsertEvent
import java.time.Instant

data class ChatRoomCreateResponse(

    val chatRoomId: Long,
    val memberId: Long,
    val targetId: Long,
    val senderId: Long,
    val nickname: String,
    val profileUrl: String?,
    val unreadCount: Long,
    val lastMessagePreview: String,
    val lastMessageAt: Instant,
) {

    companion object {
        fun from(event: ChatRoomUpsertEvent): ChatRoomCreateResponse {
            return ChatRoomCreateResponse(
                chatRoomId = event.chatRoomId,
                memberId = event.memberId,
                targetId = event.targetId,
                senderId = event.senderId,
                nickname = event.nickname,
                profileUrl = event.profileUrl,
                unreadCount = 0,
                lastMessagePreview = event.lastMessagePreview,
                lastMessageAt = event.lastMessageAt
            )
        }
    }
}
