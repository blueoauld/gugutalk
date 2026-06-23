package com.blueoauld.server.member.presentation

import com.blueoauld.server.member.application.MemberImageService
import com.blueoauld.server.r2.application.response.UploadUrlResponses
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(MemberImageController::class)
@Import(MemberImageControllerTest.Mocks::class)
class MemberImageControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun memberImageService(): MemberImageService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var memberImageService: MemberImageService

    @Test
    fun `공개 이미지 업로드 URL 발급은 200을 반환한다`() {
        every { memberImageService.createPublicUploadUrls(eq(1L), any()) } returns UploadUrlResponses(emptyList())

        mockMvc.perform(
            post("/api/members/images/public/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"urls":[{"contentType":"image/jpeg","contentLength":1024}]}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)
    }

    @Test
    fun `URL 목록이 비어 있으면 INVALID_INPUT 코드와 400을 반환한다`() {
        mockMvc.perform(
            post("/api/members/images/private/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"urls":[]}""")
                .with(withLogin(1L))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
    }
}
