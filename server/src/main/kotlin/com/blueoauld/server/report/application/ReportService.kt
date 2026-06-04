package com.blueoauld.server.report.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.MEMBER_01
import com.blueoauld.server.common.exception.type.ErrorCode.REPORT_01
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.report.application.event.ReportCreateEvent
import com.blueoauld.server.report.application.event.ReportNotifyEvent
import com.blueoauld.server.report.application.request.ReportCreateRequest
import com.blueoauld.server.report.entity.Report
import com.blueoauld.server.report.entity.ReportImage
import com.blueoauld.server.report.repository.ReportImageRepository
import com.blueoauld.server.report.repository.ReportRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(

    private val reportRepository: ReportRepository,
    private val reportImageRepository: ReportImageRepository,
    private val memberRepository: MemberRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val r2Properties: R2Properties
) {

    @Transactional
    fun create(memberId: Long, targetId: Long, request: ReportCreateRequest) {
        if (memberId == targetId) {
            throw CustomException(REPORT_01)
        }

        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val target = memberRepository.findByIdOrNull(targetId) ?: throw CustomException(MEMBER_01)

        val report = Report(
            fromId = member.id,
            fromPhone = member.phone,
            fromNickname = member.nickname,
            toId = target.id,
            toPhone = target.phone,
            toNickname = target.nickname,
            type = request.type,
            reason = request.reason,
        )
        reportRepository.save(report)

        val reportImages = request.images.map {
            val fileName = it.key.substringAfterLast("/")

            ReportImage(
                reportId = report.id,
                key = "report/${report.id}/$fileName",
                url = "${r2Properties.domain}/report/${report.id}/$fileName"
            )
        }
        reportImageRepository.saveAll(reportImages)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ReportCreateEvent(
                reportId = report.id,
                keys = request.images.map { it.key }
            )
        )

        applicationEventPublisher.publishEvent(
            ReportNotifyEvent.from(report, reportImages)
        )
    }
}