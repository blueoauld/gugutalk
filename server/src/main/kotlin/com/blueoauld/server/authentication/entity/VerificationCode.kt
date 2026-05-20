package com.blueoauld.server.authentication.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
class VerificationCode(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "phone", nullable = false)
    val phone: String,

    @Column(name = "device_id", nullable = false)
    val deviceId: String,

    @Column(name = "ip", nullable = false)
    val ip: String,

    @Column(name = "code", nullable = false)
    val code: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)