package com.blueoauld.server.report.repository

import com.blueoauld.server.report.entity.Report
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ReportRepository : JpaRepository<Report, Long> {

    @Query(
        value = """
            SELECT id
            FROM report
            WHERE created_at < :threshold
            ORDER BY id
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findAllByExpiredIds(
        @Param("threshold") threshold: Instant,
        @Param("limit") limit: Int,
    ): List<Long>

    fun deleteByIdIn(ids: List<Long>): Int
}