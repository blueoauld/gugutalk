package com.blueoauld.server.report.application

import com.blueoauld.server.report.repository.ReportImageRepository
import com.blueoauld.server.report.repository.ReportRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportCleanupService(

    private val reportRepository: ReportRepository,
    private val reportImageRepository: ReportImageRepository,
) {

    @Transactional
    fun deleteBatch(ids: List<Long>): List<String> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        val imageKeys = reportImageRepository.findKeysByReportIds(ids)

        reportImageRepository.deleteByReportIdIn(ids)
        reportRepository.deleteByIdIn(ids)
        return imageKeys
    }
}