package com.blueoauld.server.authentication.presentation

import com.blueoauld.server.authentication.application.AuthenticationFacade
import com.blueoauld.server.authentication.application.request.*
import com.blueoauld.server.authentication.application.response.LoginResponse
import com.blueoauld.server.authentication.application.response.RotateTokenResponse
import com.blueoauld.server.authentication.application.response.SignupResponse
import com.blueoauld.server.common.authentication.annotation.Login
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api")
@RestController
class AuthenticationController(

    private val authenticationFacade: AuthenticationFacade,
) {

    @PostMapping("/authentication/verify", version = "1")
    fun sendVerificationCode(
        @Valid @RequestBody request: SendVerificationCodeRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        authenticationFacade.sendVerificationCode(request, servletRequest)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentication/signup", version = "1")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ResponseEntity<SignupResponse> {
        val response = authenticationFacade.signup(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/authentication/setup", version = "1")
    fun setup(
        @Login memberId: Long,
        @Valid @RequestBody request: SetupRequest,
    ): ResponseEntity<Unit> {
        authenticationFacade.setup(memberId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentication/login", version = "1")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val response = authenticationFacade.login(request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/authentication/logout", version = "1")
    fun logout(
        @Login memberId: Long,
        @Valid @RequestBody request: LogoutRequest,
    ): ResponseEntity<Unit> {
        authenticationFacade.logout(memberId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/authentication/token/rotate", version = "1")
    fun rotateToken(
        @Valid @RequestBody request: RotateTokenRequest,
    ): ResponseEntity<RotateTokenResponse> {
        val response = authenticationFacade.rotateToken(request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/authentication/account", version = "1")
    fun delete(
        @Login memberId: Long,
        @Valid @RequestBody request: DeleteAccountRequest,
    ): ResponseEntity<Unit> {
        authenticationFacade.deleteAccount(memberId, request)
        return ResponseEntity.ok().build()
    }
}