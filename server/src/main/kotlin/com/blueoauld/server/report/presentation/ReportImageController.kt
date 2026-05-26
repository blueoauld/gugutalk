package com.blueoauld.server.report.presentation

import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponses
import com.blueoauld.server.report.application.ReportImageService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class ReportImageController(

    private val reportImageService: ReportImageService,
) {

    @PostMapping("/reports/images/urls", version = "1")
    fun createUploadUrls(
        @Valid @RequestBody requests: UploadUrlRequests
    ): ResponseEntity<UploadUrlResponses> {
        val responses = reportImageService.createUploadUrl(requests)
        return ResponseEntity.ok(responses)
    }
}