package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.ReviewService
import com.blueoauld.server.activity.application.response.ReviewCreateResponse
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.support.ControllerSliceTest
import com.blueoauld.server.support.WebMvcTestSupport.withLogin
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@ControllerSliceTest(ReviewController::class)
@Import(ReviewControllerTest.Mocks::class)
class ReviewControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun reviewService(): ReviewService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Test
    fun `리뷰 작성은 유효한 본문이면 200과 작성 결과를 반환한다`() {
        every { reviewService.create(1L, 2L, any()) } returns ReviewCreateResponse(
            reviewId = 10L, fromId = 1L, toId = 2L, nickname = "익명",
            content = "좋은 사람이에요", createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        mockMvc.perform(
            post("/api/reviews/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"좋은 사람이에요"}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.reviewId").value(10))
    }

    @Test
    fun `리뷰 내용이 비어 있으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            post("/api/reviews/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":""}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
    }

    @Test
    fun `자기 자신에게 작성하면 ACTIVITY_09 코드와 400을 반환한다`() {
        every { reviewService.create(1L, 1L, any()) } throws CustomException(ErrorCode.ACTIVITY_09)

        mockMvc.perform(
            post("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"내용"}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("ACTIVITY_09"))
    }

    @Test
    fun `본인이 받은 리뷰가 아니면 ACTIVITY_11 코드와 400을 반환한다`() {
        every { reviewService.delete(1L, 10L) } throws CustomException(ErrorCode.ACTIVITY_11)

        mockMvc.perform(delete("/api/reviews/10").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("ACTIVITY_11"))
    }

    @Test
    fun `리뷰 목록은 200과 커서 응답을 반환한다`() {
        every { reviewService.gets(2L, null, null, 20) } returns
                CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/reviews/2").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }
}
