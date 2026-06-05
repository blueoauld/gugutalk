package com.blueoauld.server.push.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
class Push(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "token", unique = true, nullable = false)
    val token: String,

    @Column(name = "member_id", nullable = false)
    var memberId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {

    fun touch(memberId: Long) {
        this.memberId = memberId
        this.updatedAt = Instant.now()
    }
}