package com.blueoauld.server.report.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.report.application.ReportService
import com.blueoauld.server.report.application.request.ReportCreateRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class ReportController(

    private val reportService: ReportService
) {

    @PostMapping("/reports/{targetId}", version = "1")
    fun createUploadUrls(
        @Login memberId: Long,
        @PathVariable targetId: Long,
        @Valid @RequestBody request: ReportCreateRequest
    ): ResponseEntity<Unit> {
        reportService.create(memberId, targetId, request)
        return ResponseEntity.ok().build()
    }
}