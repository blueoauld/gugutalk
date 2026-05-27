package com.blueoauld.server.member.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.member.application.MemberImageService
import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponses
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class MemberImageController(

    private val memberImageService: MemberImageService,
) {

    @PostMapping("/members/images/public/urls", version = "1")
    fun createPublicUploadUrls(
        @Login memberId: Long,
        @Valid @RequestBody requests: UploadUrlRequests
    ): ResponseEntity<UploadUrlResponses> {
        val responses = memberImageService.createPublicUploadUrls(memberId, requests)
        return ResponseEntity.ok(responses)
    }

    @PostMapping("/members/images/private/urls", version = "1")
    fun createPrivateUploadUrls(
        @Login memberId: Long,
        @Valid @RequestBody requests: UploadUrlRequests
    ): ResponseEntity<UploadUrlResponses> {
        val responses = memberImageService.createPrivateUploadUrls(memberId, requests)
        return ResponseEntity.ok(responses)
    }
}