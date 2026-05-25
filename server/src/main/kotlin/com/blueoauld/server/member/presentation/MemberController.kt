package com.blueoauld.server.member.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.member.application.MemberService
import com.blueoauld.server.member.application.request.UpdateCommentRequest
import com.blueoauld.server.member.application.response.MemberGetResponse
import com.blueoauld.server.member.application.response.MemberRowResponse
import com.blueoauld.server.member.application.response.MemberSearchRowResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

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

    @GetMapping("/members/{targetId}", version = "1")
    fun get(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<MemberGetResponse> {
        val response = memberService.get(memberId, targetId)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/members", version = "1")
    fun gets(
        @Login memberId: Long,
        @RequestParam(defaultValue = "ALL") gender: String,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<MemberRowResponse>> {
        val response = memberService.gets(memberId, gender, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/members/region", version = "1")
    fun getsByRegion(
        @Login memberId: Long,
        @RequestParam(defaultValue = "ALL") gender: String,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<MemberRowResponse>> {
        val response = memberService.getsByRegion(memberId, gender, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/members/search", version = "1")
    fun search(
        @Login memberId: Long,
        @RequestParam nickname: String,
        @RequestParam(required = false) cursorId: Long?,
        @RequestParam(required = false) cursorDateAt: Instant?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<CursorResponse<MemberSearchRowResponse>> {
        val response = memberService.search(memberId, nickname, cursorId, cursorDateAt, size)
        return ResponseEntity.ok(response)
    }
}