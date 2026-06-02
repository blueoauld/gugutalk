package com.blueoauld.server.point.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
class Point(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "member_id", unique = true, nullable = false)
    val memberId: Long,

    @Column(name = "balance", nullable = false)
    var balance: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {

    fun earn(amount: Long) {
        this.balance += amount
        this.updatedAt = Instant.now()
    }

    fun use(amount: Long) {
        this.balance -= amount
        this.updatedAt = Instant.now()
    }
}