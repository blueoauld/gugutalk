package com.blueoauld.server.report.entity

import com.blueoauld.server.report.entity.type.ReportType
import jakarta.persistence.*
import java.time.Instant

@Entity
class Report(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "from_id", nullable = false)
    val fromId: Long,

    @Column(name = "from_phone", nullable = false)
    val fromPhone: String,

    @Column(name = "from_nickname", nullable = false)
    val fromNickname: String,

    @Column(name = "to_id", nullable = false)
    val toId: Long,

    @Column(name = "to_phone", nullable = false)
    val toPhone: String,

    @Column(name = "to_nickname", nullable = false)
    val toNickname: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ReportType,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)