package com.blueoauld.server.member.presentation

import com.blueoauld.server.common.authentication.annotation.Login
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.member.application.MemberService
import com.blueoauld.server.member.application.request.MemberUpdateCommentRequest
import com.blueoauld.server.member.application.request.MemberUpdateProfileRequest
import com.blueoauld.server.member.application.response.*
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
        @Valid @RequestBody request: MemberUpdateCommentRequest
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

    @GetMapping("/members/me", version = "1")
    fun getMe(
        @Login memberId: Long,
    ): ResponseEntity<MemberGetMeResponse> {
        val response = memberService.getMe(memberId)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/members/profile", version = "1")
    fun updateProfile(
        @Login memberId: Long,
        @Valid @RequestBody request: MemberUpdateProfileRequest
    ): ResponseEntity<Unit> {
        memberService.updateProfile(memberId, request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/members/{targetId}/private-images", version = "1")
    fun getPrivateImages(
        @Login memberId: Long,
        @PathVariable targetId: Long
    ): ResponseEntity<MemberGetPrivateImagesResponse> {
        val response = memberService.getPrivateImages(memberId, targetId)
        return ResponseEntity.ok(response)
    }
}