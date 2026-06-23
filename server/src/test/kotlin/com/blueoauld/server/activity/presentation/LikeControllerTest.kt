package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.LikeService
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

@ControllerSliceTest(LikeController::class)
@Import(LikeControllerTest.Mocks::class)
class LikeControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun likeService(): LikeService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var likeService: LikeService

    @Test
    fun `좋아요 생성은 200을 반환한다`() {
        every { likeService.create(1L, 2L) } returns Unit

        mockMvc.perform(post("/api/likes/2").with(withLogin(1L)))
            .andExpect(status().isOk)

        verify { likeService.create(1L, 2L) }
    }

    @Test
    fun `이미 좋아요한 대상이면 ACTIVITY_01 코드와 409를 반환한다`() {
        every { likeService.create(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_01)

        mockMvc.perform(post("/api/likes/2").with(withLogin(1L)))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("ACTIVITY_01"))
    }

    @Test
    fun `좋아요 취소는 200을 반환한다`() {
        every { likeService.delete(1L, 2L) } returns Unit

        mockMvc.perform(delete("/api/likes/2").with(withLogin(1L)))
            .andExpect(status().isOk)
    }

    @Test
    fun `좋아요한 적이 없으면 ACTIVITY_02 코드와 400을 반환한다`() {
        every { likeService.delete(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_02)

        mockMvc.perform(delete("/api/likes/2").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("ACTIVITY_02"))
    }

    @Test
    fun `좋아요 목록은 200과 커서 응답을 반환한다`() {
        every { likeService.gets(1L, null, null, 20) } returns
            CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/likes").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }
}
