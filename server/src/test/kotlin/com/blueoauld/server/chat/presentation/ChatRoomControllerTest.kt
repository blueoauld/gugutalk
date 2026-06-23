package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatRoomService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(ChatRoomController::class)
@Import(ChatRoomControllerTest.Mocks::class)
class ChatRoomControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun chatRoomService(): ChatRoomService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var chatRoomService: ChatRoomService

    @Test
    fun `채팅방 생성은 유효한 본문이면 200을 반환한다`() {
        every { chatRoomService.create(1L, 2L, any()) } returns Unit

        mockMvc.perform(
            post("/api/chat-rooms/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"안녕하세요"}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { chatRoomService.create(1L, 2L, any()) }
    }

    @Test
    fun `쪽지 내용이 비어 있으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            post("/api/chat-rooms/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":""}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
    }

    @Test
    fun `차단 관계면 CHAT_05 코드와 400을 반환한다`() {
        every { chatRoomService.create(1L, 2L, any()) } throws CustomException(ErrorCode.CHAT_05)

        mockMvc.perform(
            post("/api/chat-rooms/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"안녕"}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("CHAT_05"))
    }

    @Test
    fun `존재하지 않는 채팅방 삭제는 CHAT_03 코드와 400을 반환한다`() {
        every { chatRoomService.delete(1L, 10L) } throws CustomException(ErrorCode.CHAT_03)

        mockMvc.perform(delete("/api/chat-rooms/10").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("CHAT_03"))
    }

    @Test
    fun `읽음 처리는 200을 반환한다`() {
        every { chatRoomService.read(1L, 10L) } returns Unit

        mockMvc.perform(patch("/api/chat-rooms/10/read").with(withLogin(1L)))
            .andExpect(status().isOk)
    }

    @Test
    fun `채팅방 목록은 200과 커서 응답을 반환한다`() {
        every { chatRoomService.gets(1L, "ALL", null, null, 20) } returns
                CursorResponse(payload = emptyList(), nextId = null, nextDateAt = null, hasNext = false)

        mockMvc.perform(get("/api/chat-rooms").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hasNext").value(false))
    }
}
