package com.blueoauld.server.report.repository

import com.blueoauld.server.report.entity.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long>