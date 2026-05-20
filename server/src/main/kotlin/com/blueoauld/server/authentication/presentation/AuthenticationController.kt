package com.blueoauld.server.authentication.presentation

import com.blueoauld.server.authentication.application.AuthenticationService
import com.blueoauld.server.authentication.application.request.SendVerificationCodeRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api")
@RestController
class AuthenticationController(

    private val authenticationService: AuthenticationService,
) {

    @PostMapping("/authentications/send-verification-code")
    fun sendVerificationCode(
        @RequestBody request: SendVerificationCodeRequest,
        servletRequest: HttpServletRequest,
    ): ResponseEntity<Unit> {
        authenticationService.sendVerificationCode(request, servletRequest)
        return ResponseEntity.ok().build()
    }
}