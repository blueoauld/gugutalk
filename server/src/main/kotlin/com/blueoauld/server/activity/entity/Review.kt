package com.blueoauld.server.activity.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
class Review(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "from_id", nullable = false)
    val fromId: Long,

    @Column(name = "to_id", nullable = false)
    val toId: Long,

    @Column(name = "content", length = 100, nullable = false)
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)