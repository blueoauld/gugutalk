package com.blueoauld.server.authentication.presentation

import com.blueoauld.server.authentication.application.AuthenticationService
import com.blueoauld.server.authentication.application.request.*
import com.blueoauld.server.authentication.application.response.LoginResponse
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.common.authentication.annotation.Login
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class AuthenticationController(

    private val authenticationService: AuthenticationService,
) {

    @PostMapping("/authentications/verification-code", version = "1")
    fun sendVerificationCode(
        @Valid @RequestBody request: SendVerificationCodeRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        authenticationService.sendVerificationCode(request, servletRequest)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentications/signup", version = "1")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<SignupResponse> {
        val response = authenticationService.signup(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/authentications/setup", version = "1")
    fun setup(
        @Login memberId: Long,
        @Valid @RequestBody request: SetupRequest,
    ): ResponseEntity<Unit> {
        authenticationService.setup(memberId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentications/login", version = "1")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val response = authenticationService.login(request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/authentications/logout", version = "1")
    fun logout(
        @Login memberId: Long,
        @Valid @RequestBody request: LogoutRequest,
    ): ResponseEntity<Unit> {
        authenticationService.logout(memberId, request)
        return ResponseEntity.ok().build()
    }
}