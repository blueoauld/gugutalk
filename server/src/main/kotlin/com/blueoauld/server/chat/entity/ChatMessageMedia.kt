package com.blueoauld.server.chat.entity

import com.blueoauld.server.chat.entity.type.MessageType
import jakarta.persistence.*
import java.time.Instant

@Entity
class ChatMessageMedia(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "chat_message_id", nullable = false)
    val chatMessageId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: MessageType,

    @Column(name = "url", nullable = false)
    val url: String,

    @Column(name = "key", nullable = false)
    val key: String,

    @Column(name = "thumbnail_url")
    val thumbnailUrl: String? = null,

    @Column(name = "thumbnail_key")
    val thumbnailKey: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)