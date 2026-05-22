package com.blueoauld.server.activity.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "unlike",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_unlike_from_to",
            columnNames = ["from_id", "to_id"]
        )
    ]
)
class Unlike(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "from_id", nullable = false)
    val fromId: Long,

    @Column(name = "to_id", nullable = false)
    val toId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)