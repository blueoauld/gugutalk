package com.blueoauld.server.push.presentation

import com.blueoauld.server.push.application.PushService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(PushController::class)
@Import(PushControllerTest.Mocks::class)
class PushControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun pushService(): PushService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pushService: PushService

    @Test
    fun `디바이스 토큰 등록은 200을 반환한다`() {
        every { pushService.upsert(1L, "token-1") } returns Unit

        mockMvc.perform(post("/api/push").param("token", "token-1").with(withLogin(1L)))
            .andExpect(status().isOk)

        verify { pushService.upsert(1L, "token-1") }
    }

    @Test
    fun `디바이스 토큰 삭제는 인증 없이 200을 반환한다`() {
        every { pushService.delete("token-1") } returns Unit

        mockMvc.perform(delete("/api/push").param("token", "token-1"))
            .andExpect(status().isOk)

        verify { pushService.delete("token-1") }
    }
}
