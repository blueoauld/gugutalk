package com.blueoauld.server.ban.entity

import com.blueoauld.server.ban.entity.type.BanType
import jakarta.persistence.*
import java.time.Instant

@Entity
class BanHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "banId", nullable = false)
    val banId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: BanType,

    @Column(name = "target", nullable = false)
    val target: String,

    @Column(name = "phone")
    val phone: String? = null,

    @Column(name = "deviceId")
    val deviceId: String? = null,

    @Column(name = "nickname")
    val nickname: String? = null,

    @Column(name = "reason", nullable = false)
    val reason: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expired_at", nullable = false)
    val expiredAt: Instant,
)