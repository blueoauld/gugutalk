package com.blueoauld.server.authentication.presentation

import com.blueoauld.server.authentication.application.AuthenticationFacade
import com.blueoauld.server.authentication.application.response.LoginResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.support.ControllerSliceTest
import com.blueoauld.server.support.WebMvcTestSupport.withLogin
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(AuthenticationController::class)
@Import(AuthenticationControllerTest.Mocks::class)
class AuthenticationControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun authenticationFacade(): AuthenticationFacade = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authenticationFacade: AuthenticationFacade

    @Test
    fun `인증 번호 발송은 유효한 본문이면 200을 반환한다`() {
        every { authenticationFacade.sendVerificationCode(any(), any()) } just Runs

        mockMvc.perform(
            post("/api/authentication/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"phone":"01012345678","deviceId":"dev-1"}""")
        ).andExpect(status().isOk)

        verify { authenticationFacade.sendVerificationCode(any(), any()) }
    }

    @Test
    fun `휴대폰 번호 형식이 올바르지 않으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            post("/api/authentication/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"phone":"123","deviceId":"dev-1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
    }

    @Test
    fun `로그인은 성공하면 200과 토큰을 반환한다`() {
        every { authenticationFacade.login(any()) } returns LoginResponse(
            memberId = 1L, accessToken = "access", refreshToken = "refresh"
        )

        mockMvc.perform(
            post("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"phone":"01012345678","password":"pw","deviceId":"dev-1"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("access"))
    }

    @Test
    fun `휴대폰 또는 비밀번호가 틀리면 MEMBER_05 코드와 400을 반환한다`() {
        every { authenticationFacade.login(any()) } throws CustomException(ErrorCode.MEMBER_05)

        mockMvc.perform(
            post("/api/authentication/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"phone":"01012345678","password":"wrong","deviceId":"dev-1"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("MEMBER_05"))
    }

    @Test
    fun `회원 탈퇴는 로그인 상태면 200을 반환한다`() {
        every { authenticationFacade.deleteAccount(1L) } just Runs

        mockMvc.perform(delete("/api/authentication/account").with(withLogin(1L)))
            .andExpect(status().isOk)

        verify { authenticationFacade.deleteAccount(1L) }
    }
}
