package com.blueoauld.server.authentication.presentation

import com.blueoauld.server.authentication.application.AuthenticationService
import com.blueoauld.server.authentication.application.request.SendVerificationCodeRequest
import com.blueoauld.server.authentication.application.request.SetupRequest
import com.blueoauld.server.authentication.application.request.SignupRequest
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.common.authentication.annotation.Login
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class AuthenticationController(

    private val authenticationService: AuthenticationService,
) {

    @PostMapping("/authentications/verification-code")
    fun sendVerificationCode(
        @RequestBody request: SendVerificationCodeRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        authenticationService.sendVerificationCode(request, servletRequest)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentications/signup")
    fun signup(
        @RequestBody request: SignupRequest,
    ): ResponseEntity<SignupResponse> {
        val response = authenticationService.signup(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/authentications/setup")
    fun setup(
        @Login memberId: Long,
        @RequestBody request: SetupRequest,
    ): ResponseEntity<Unit> {
        authenticationService.setup(memberId, request)
        return ResponseEntity.ok().build()
    }
}