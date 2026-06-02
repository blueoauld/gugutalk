package com.blueoauld.server.point.entity

import com.blueoauld.server.point.entity.type.PointSource
import jakarta.persistence.*
import java.time.Instant

@Entity
class PointHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "point_id", nullable = false)
    val pointId: Long,

    @Column(name = "source", nullable = false)
    var source: PointSource,

    @Column(name = "balance_snapshot", nullable = false)
    var balanceSnapshot: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)