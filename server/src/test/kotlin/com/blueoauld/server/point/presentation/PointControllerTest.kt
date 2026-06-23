package com.blueoauld.server.point.presentation

import com.blueoauld.server.common.authentication.filter.AuthenticationFilter
import com.blueoauld.server.common.authentication.infrastructure.AuthenticationPrincipalArgumentResolver
import com.blueoauld.server.common.configuration.WebConfiguration
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.GlobalExceptionHandler
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.common.filter.RequestLoggingFilter
import com.blueoauld.server.point.application.PointService
import com.blueoauld.server.point.application.response.PointGetBalanceResponse
import com.blueoauld.server.support.WebMvcTestSupport.withLogin
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [PointController::class],
    excludeFilters = [
        Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [AuthenticationFilter::class, RequestLoggingFilter::class],
        ),
    ],
)
@Import(
    WebConfiguration::class,
    AuthenticationPrincipalArgumentResolver::class,
    GlobalExceptionHandler::class,
    PointControllerTest.Mocks::class,
)
class PointControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun pointService(): PointService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pointService: PointService

    @Test
    fun `잔액 조회는 200과 잔액을 반환한다`() {
        every { pointService.getBalance(1L) } returns PointGetBalanceResponse(balance = 500L)

        mockMvc.perform(get("/api/points/balance").with(withLogin(1L)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(500))
    }

    @Test
    fun `포인트 계정이 없으면 POINT_01 코드와 400을 반환한다`() {
        every { pointService.getBalance(1L) } throws CustomException(ErrorCode.POINT_01)

        mockMvc.perform(get("/api/points/balance").with(withLogin(1L)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("POINT_01"))
    }

    @Test
    fun `출석 보상은 X-Device-Id 헤더와 함께 200을 반환한다`() {
        every { pointService.rewardAttendance(1L, "device-1") } returns Unit

        mockMvc.perform(
            post("/api/points/attendance")
                .header("X-Device-Id", "device-1")
                .with(withLogin(1L))
        ).andExpect(status().isOk)

        verify { pointService.rewardAttendance(1L, "device-1") }
    }

    @Test
    fun `이미 출석했으면 POINT_02 코드와 409를 반환한다`() {
        every { pointService.rewardAttendance(1L, "device-1") } throws CustomException(ErrorCode.POINT_02)

        mockMvc.perform(
            post("/api/points/attendance")
                .header("X-Device-Id", "device-1")
                .with(withLogin(1L))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("POINT_02"))
    }
}
