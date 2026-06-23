package com.blueoauld.server.point.presentation

import com.blueoauld.server.point.application.RewardService
import com.blueoauld.server.support.ControllerSliceTest
import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ControllerSliceTest(RewardController::class)
@Import(RewardControllerTest.Mocks::class)
class RewardControllerTest {

    @TestConfiguration
    class Mocks {
        @Bean
        fun rewardedAdsVerifier(): RewardedAdsVerifier = mockk()

        @Bean
        fun rewardService(): RewardService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var rewardedAdsVerifier: RewardedAdsVerifier

    @Autowired
    private lateinit var rewardService: RewardService

    @BeforeEach
    fun reset() {
        clearMocks(rewardedAdsVerifier, rewardService)
    }

    @Test
    fun `SSV 서명이 유효하면 보상을 지급하고 200을 반환한다`() {
        every { rewardedAdsVerifier.verify(any()) } just Runs
        every { rewardService.grantReward(any(), any()) } just Runs

        mockMvc.perform(get("/admob-ssv").param("transaction_id", "tx-1").param("user_id", "100"))
            .andExpect(status().isOk)

        verify { rewardService.grantReward("tx-1", "100") }
    }

    @Test
    fun `SSV 서명 검증에 실패하면 403을 반환하고 보상을 지급하지 않는다`() {
        every { rewardedAdsVerifier.verify(any()) } throws RuntimeException("invalid signature")

        mockMvc.perform(get("/admob-ssv").param("transaction_id", "tx-1"))
            .andExpect(status().isForbidden)

        verify(exactly = 0) { rewardService.grantReward(any(), any()) }
    }
}
