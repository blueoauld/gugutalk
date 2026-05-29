package com.blueoauld.server.chat.entity

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.CHAT_01
import com.blueoauld.server.common.exception.type.ErrorCode.CHAT_02
import jakarta.persistence.*
import org.hibernate.annotations.SoftDelete
import org.hibernate.annotations.SoftDeleteType
import java.time.Instant

@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Entity
class ChatRoom(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "member1_id", nullable = false)
    val member1Id: Long,

    @Column(name = "member2_id", nullable = false)
    val member2Id: Long,

    @Column(name = "member1_last_read_message_id", nullable = false)
    var member1LastReadMessageId: Long,

    @Column(name = "member2_last_read_message_id", nullable = false)
    var member2LastReadMessageId: Long,

    @Column(name = "last_message_preview", nullable = false)
    var lastMessagePreview: String,

    @Column(name = "last_message_at", nullable = false)
    var lastMessageAt: Instant,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
) {

    companion object {
        fun create(member1Id: Long, member2Id: Long): ChatRoom {
            if (member1Id == member2Id) {
                throw CustomException(CHAT_01)
            }

            return ChatRoom(
                member1Id = minOf(member1Id, member2Id),
                member2Id = maxOf(member1Id, member2Id),
                member1LastReadMessageId = 0L,
                member2LastReadMessageId = 0L,
                lastMessagePreview = "",
                lastMessageAt = Instant.now(),
            )
        }
    }

    fun markAsRead(memberId: Long, chatMessageId: Long) {
        when (memberId) {
            member1Id -> member1LastReadMessageId = maxOf(member1LastReadMessageId, chatMessageId)
            member2Id -> member2LastReadMessageId = maxOf(member2LastReadMessageId, chatMessageId)
            else -> throw CustomException(CHAT_02)
        }
    }

    fun onMessageSent(senderId: Long, message: ChatMessage) {
        markAsRead(senderId, message.id)

        this.lastMessagePreview = message.content.take(100)
        this.lastMessageAt = message.createdAt
    }
}