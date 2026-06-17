package com.blueoauld.server.chat.entity

import com.blueoauld.server.chat.entity.type.ReactionType
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chat_message_reaction_chat_message_id_member_id",
            columnNames = ["chat_message_id", "member_id"]
        )
    ]
)
class ChatMessageReaction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "chat_message_id", nullable = false)
    val chatMessageId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ReactionType,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
) {

    fun changeType(type: ReactionType) {
        this.type = type
    }
}