package com.blueoauld.server.member.presentation

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.member.application.MemberService
import com.blueoauld.server.support.ControllerSliceTest
import com.blueoauld.server.support.WebMvcTestSupport.withLogin
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(MemberController::class)
@Import(MemberControllerTest.Mocks::class)
class MemberControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun memberService(): MemberService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberService: MemberService

    @Test
    fun `코멘트 수정은 유효한 본문이면 200을 반환한다`() {
        every { memberService.updateComment(1L, any()) } returns Unit

        mockMvc.perform(
            patch("/api/members/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"반갑습니다"}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { memberService.updateComment(1L, any()) }
    }

    @Test
    fun `코멘트가 비어 있으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            patch("/api/members/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":""}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))

        verify(exactly = 0) { memberService.updateComment(any(), any()) }
    }

    @Test
    fun `존재하지 않는 회원 조회는 MEMBER_01 코드와 404를 반환한다`() {
        every { memberService.get(1L, 2L) } throws CustomException(ErrorCode.MEMBER_01)

        mockMvc.perform(get("/api/members/2").with(withLogin(1L)))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("MEMBER_01"))
    }
}
