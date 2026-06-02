package com.blueoauld.server.point.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.point.application.PointService
import com.blueoauld.server.point.application.response.PointGetBalanceResponse
import com.blueoauld.server.point.application.response.PointHistoryRowResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RequestMapping("/api")
@RestController
class PointController(

    private val pointService: PointService,
) {

    @GetMapping("/points/balance", version = "1")
    fun getBalance(
        @Login memberId: Long
    ): ResponseEntity<PointGetBalanceResponse> {
        val response = pointService.getBalance(memberId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/points/attendance", version = "1")
    fun rewardAttendance(
        @Login memberId: Long
    ): ResponseEntity<Unit> {
        pointService.rewardAttendance(memberId)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/points/advertisement", version = "1")
    fun rewardAdvertisement(
        @Login memberId: Long
    ): ResponseEntity<Unit> {
        pointService.rewardAdvertisement(memberId)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/points", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<PointHistoryRowResponse>> {
        val response = pointService.gets(memberId, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}