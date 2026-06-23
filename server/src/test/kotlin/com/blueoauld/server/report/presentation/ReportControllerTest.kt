package com.blueoauld.server.report.presentation

import com.blueoauld.server.report.application.ReportImageService
import com.blueoauld.server.report.application.ReportService
import com.blueoauld.server.r2.application.response.UploadUrlResponses
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(ReportController::class, ReportImageController::class)
@Import(ReportControllerTest.Mocks::class)
class ReportControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun reportService(): ReportService = mockk()

        @Bean
        fun reportImageService(): ReportImageService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reportService: ReportService

    @Autowired
    private lateinit var reportImageService: ReportImageService

    @Test
    fun `신고 생성은 유효한 본문이면 200을 반환한다`() {
        every { reportService.create(eq(1L), eq(2L), any()) } returns Unit

        mockMvc.perform(
            post("/api/reports/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"type":"ABUSE","reason":"욕설을 했습니다","images":[]}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { reportService.create(eq(1L), eq(2L), any()) }
    }

    @Test
    fun `신고 이미지 업로드 URL 발급은 200을 반환한다`() {
        every { reportImageService.createUploadUrls(any()) } returns UploadUrlResponses(emptyList())

        mockMvc.perform(
            post("/api/reports/images/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"urls":[{"contentType":"image/jpeg","contentLength":1024}]}""")
                .with(withLogin(1L))
        ).andExpect(status().isOk)
    }
}
