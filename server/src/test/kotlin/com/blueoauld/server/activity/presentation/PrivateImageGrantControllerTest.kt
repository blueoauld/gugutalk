package com.blueoauld.server.activity.presentation

import com.blueoauld.server.activity.application.PrivateImageGrantService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(PrivateImageGrantController::class)
@Import(PrivateImageGrantControllerTest.Mocks::class)
class PrivateImageGrantControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun privateImageGrantService(): PrivateImageGrantService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var privateImageGrantService: PrivateImageGrantService

    @Test
    fun `비밀 사진 공개는 200을 반환한다`() {
        every { privateImageGrantService.create(1L, 2L) } returns Unit

        mockMvc.perform(post("/api/private-image-grants/2").with(withLogin(1L)))
            .andExpect(status().isOk)

        verify { privateImageGrantService.create(1L, 2L) }
    }

    @Test
    fun `이미 공개한 대상이면 ACTIVITY_07 코드와 409를 반환한다`() {
        every { privateImageGrantService.create(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_07)

        mockMvc.perform(post("/api/private-image-grants/2").with(withLogin(1L)))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("ACTIVITY_07"))
    }

    @Test
    fun `공개한 적이 없으면 ACTIVITY_08 코드와 400을 반환한다`() {
        every { privateImageGrantService.delete(1L, 2L) } throws CustomException(ErrorCode.ACTIVITY_08)

        mockMvc.perform(delete("/api/private-image-grants/2").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("ACTIVITY_08"))
    }

    @Test
    fun `공개 목록은 200과 커서 응답을 반환한다`() {
        every { privateImageGrantService.gets(1L, null, null, 20) } returns
                CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/private-image-grants").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }
}
