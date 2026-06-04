package com.blueoauld.server.report.application.event

import com.blueoauld.server.report.entity.Report
import com.blueoauld.server.report.entity.ReportImage
import com.blueoauld.server.report.entity.type.ReportType
import java.time.Instant

data class ReportNotifyEvent(

    val reportId: Long,
    val fromMemberId: Long,
    val fromPhone: String,
    val fromNickname: String,
    val toMemberId: Long,
    val toPhone: String,
    val toNickname: String,
    val type: ReportType,
    val reason: String,
    val createdAt: Instant,
    val images: List<String>,
) {

    companion object {
        fun from(report: Report, reportImages: List<ReportImage>): ReportNotifyEvent {
            return ReportNotifyEvent(
                reportId = report.id,
                fromMemberId = report.fromId,
                fromPhone = report.fromPhone,
                fromNickname = report.fromNickname,
                toMemberId = report.toId,
                toPhone = report.toPhone,
                toNickname = report.toNickname,
                type = report.type,
                reason = report.reason,
                createdAt = report.createdAt,
                images = reportImages.map { it.url }
            )
        }
    }
}
