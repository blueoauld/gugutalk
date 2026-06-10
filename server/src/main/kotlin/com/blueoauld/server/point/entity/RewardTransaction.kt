package com.blueoauld.server.point.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class RewardTransaction(

    @Id
    val id: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "amount", nullable = false)
    val amount: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)