package com.blueoauld.server.report.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
class ReportImage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "report_id", nullable = false)
    val reportId: Long,

    @Column(name = "key", unique = true, nullable = false)
    val key: String,

    @Column(name = "url", unique = true, nullable = false)
    val url: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)