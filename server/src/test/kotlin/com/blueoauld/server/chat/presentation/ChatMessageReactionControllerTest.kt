package com.blueoauld.server.chat.presentation

import com.blueoauld.server.chat.application.ChatMessageReactionService
import com.blueoauld.server.chat.entity.type.ReactionType
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(ChatMessageReactionController::class)
@Import(ChatMessageReactionControllerTest.Mocks::class)
class ChatMessageReactionControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun chatMessageReactionService(): ChatMessageReactionService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var chatMessageReactionService: ChatMessageReactionService

    @Test
    fun `반응 등록은 200을 반환한다`() {
        every { chatMessageReactionService.react(1L, 10L, 5L, ReactionType.HEART) } returns Unit

        mockMvc.perform(
            put("/api/chat-rooms/10/messages/5/reactions")
                .param("type", "HEART")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { chatMessageReactionService.react(1L, 10L, 5L, ReactionType.HEART) }
    }
}
