package com.blueoauld.server.point.presentation

import com.blueoauld.server.point.application.RewardService
import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class RewardController(

    private val rewardedAdsVerifier: RewardedAdsVerifier,
    private val rewardService: RewardService,
) {

    private val log = KotlinLogging.logger {}

    @GetMapping("/admob-ssv")
    fun handleCallback(
        request: HttpServletRequest,
        @RequestParam("user_id", required = false) userId: String?,
        @RequestParam("transaction_id") transactionId: String,
    ): ResponseEntity<Unit> {
        try {
            rewardedAdsVerifier.verify("${request.requestURL}?${request.queryString}")
        } catch (e: Exception) {
            log.warn { "SSV 서명 검증에 실패했습니다. ${e.message}" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        rewardService.grantReward(
            transactionId = transactionId,
            userId = userId,
        )
        return ResponseEntity.ok().build()
    }
}