package com.blueoauld.server.point.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.point.application.PointService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class PointController(

    private val pointService: PointService,
) {

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
}