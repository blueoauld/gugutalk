package com.blueoauld.server.member.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.member.application.MemberService
import com.blueoauld.server.member.application.request.UpdateCommentRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class MemberController(

    private val memberService: MemberService,
) {

    @PatchMapping("/members/comment", version = "1")
    fun updateComment(
        @Login memberId: Long,
        @Valid @RequestBody request: UpdateCommentRequest
    ): ResponseEntity<Unit> {
        memberService.updateComment(memberId, request)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/members/bump", version = "1")
    fun bump(
        @Login memberId: Long
    ): ResponseEntity<Unit> {
        memberService.bump(memberId)
        return ResponseEntity.ok().build()
    }
}