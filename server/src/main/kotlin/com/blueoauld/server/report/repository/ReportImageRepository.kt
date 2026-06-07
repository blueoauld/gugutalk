package com.blueoauld.server.report.repository

import com.blueoauld.server.report.entity.ReportImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReportImageRepository : JpaRepository<ReportImage, Long> {

    @Query("SELECT ri.key FROM ReportImage ri WHERE ri.reportId IN :ids")
    fun findKeysByReportIds(@Param("ids") ids: List<Long>): List<String>

    fun deleteByReportIdIn(reportIds: List<Long>): Int
}