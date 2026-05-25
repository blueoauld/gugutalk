package com.blueoauld.server.report.repository

import com.blueoauld.server.report.entity.ReportImage
import org.springframework.data.jpa.repository.JpaRepository

interface ReportImageRepository : JpaRepository<ReportImage, Long>