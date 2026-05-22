package com.blueoauld.server.activity.entity

import jakarta.persistence.*
import org.hibernate.annotations.SoftDelete
import org.hibernate.annotations.SoftDeleteType
import java.time.Instant

@SoftDelete(columnName = "deleted_at", strategy = SoftDeleteType.TIMESTAMP)
@Entity
class Review(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "from_id", nullable = false)
    val fromId: Long,

    @Column(name = "to_id", nullable = false)
    val toId: Long,

    @Column(name = "nickname", nullable = false)
    val nickname: String,

    @Column(name = "content", length = 500, nullable = false)
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)