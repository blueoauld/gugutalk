package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.UnlikeService
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(UnlikeController::class)
@Import(UnlikeControllerTest.Mocks::class)
class UnlikeControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun unlikeService(): UnlikeService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var unlikeService: UnlikeService

    @Test
    fun `싫어요 생성은 200을 반환한다`() {
        every { unlikeService.create(1L, 2L) } returns Unit

        mockMvc.perform(post("/api/unlikes/2").with(withLogin(1L)))
            .andExpect(status().isOk)

        verify { unlikeService.create(1L, 2L) }
    }

    @Test
    fun `이미 싫어요한 대상이면 ACTIVITY_03 코드와 409를 반환한다`() {
        every { unlikeService.create(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_03)

        mockMvc.perform(post("/api/unlikes/2").with(withLogin(1L)))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("ACTIVITY_03"))
    }

    @Test
    fun `싫어요한 적이 없으면 ACTIVITY_04 코드와 400을 반환한다`() {
        every { unlikeService.delete(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_04)

        mockMvc.perform(delete("/api/unlikes/2").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("ACTIVITY_04"))
    }

    @Test
    fun `싫어요 목록은 200과 커서 응답을 반환한다`() {
        every { unlikeService.gets(1L, null, null, 20) } returns
            CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/unlikes").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }
}
