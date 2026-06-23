package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatMessageService
import com.blueoauld.server.chat.application.response.ChatMessageGetVideoResponse
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
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(ChatMessageController::class)
@Import(ChatMessageControllerTest.Mocks::class)
class ChatMessageControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun chatMessageService(): ChatMessageService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var chatMessageService: ChatMessageService

    @Test
    fun `메세지 목록은 200과 커서 응답을 반환한다`() {
        every { chatMessageService.gets(1L, 10L, null, null, 50) } returns
                CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/chat-rooms/10/messages").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }

    @Test
    fun `참여자가 아니면 CHAT_02 코드와 400을 반환한다`() {
        every { chatMessageService.gets(1L, 10L, null, null, 50) } throws CustomException(ErrorCode.CHAT_02)

        mockMvc.perform(get("/api/chat-rooms/10/messages").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("CHAT_02"))
    }

    @Test
    fun `메세지 전송은 유효한 본문이면 200을 반환한다`() {
        every { chatMessageService.send(1L, 10L, any()) } returns Unit

        mockMvc.perform(
            post("/api/chat-rooms/10/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"안녕"}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { chatMessageService.send(1L, 10L, any()) }
    }

    @Test
    fun `메세지 내용이 비어 있으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            post("/api/chat-rooms/10/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":""}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
    }

    @Test
    fun `영상 조회는 200과 URL을 반환한다`() {
        every { chatMessageService.getVideo(5L) } returns ChatMessageGetVideoResponse(url = "https://cdn/v.mp4")

        mockMvc.perform(get("/api/chat-messages/5/video").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value("https://cdn/v.mp4"))
    }

    @Test
    fun `재생할 수 없는 메세지면 CHAT_MESSAGE_02 코드와 400을 반환한다`() {
        every { chatMessageService.getVideo(5L) } throws CustomException(ErrorCode.CHAT_MESSAGE_02)

        mockMvc.perform(get("/api/chat-messages/5/video").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("CHAT_MESSAGE_02"))
    }
}
