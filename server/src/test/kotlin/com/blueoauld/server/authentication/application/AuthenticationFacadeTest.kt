package com.blueoauld.server.authentication.application

import com.blueoauld.server.authentication.application.port.AccessTokenBlacklistStore
import com.blueoauld.server.authentication.application.port.MessageSender
import com.blueoauld.server.authentication.application.port.RefreshTokenStore
import com.blueoauld.server.authentication.application.port.RotatedTokenStore
import com.blueoauld.server.authentication.application.port.RotatedTokens
import com.blueoauld.server.authentication.application.port.VerificationCodeStore
import com.blueoauld.server.authentication.application.port.VerificationSendLimiter
import com.blueoauld.server.authentication.application.request.RotateTokenRequest
import com.blueoauld.server.common.authentication.application.TokenProvider
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.memberFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthenticationFacadeTest {

    private val authenticationService = mockk<AuthenticationService>()
    private val tokenProvider = mockk<TokenProvider>()
    private val verificationCodeStore = mockk<VerificationCodeStore>(relaxUnitFun = true)
    private val verificationSendLimiter = mockk<VerificationSendLimiter>(relaxUnitFun = true)
    private val refreshTokenStore = mockk<RefreshTokenStore>(relaxUnitFun = true)
    private val rotatedTokenStore = mockk<RotatedTokenStore>(relaxUnitFun = true)
    private val accessTokenBlacklistStore = mockk<AccessTokenBlacklistStore>(relaxUnitFun = true)
    private val messageSender = mockk<MessageSender>(relaxUnitFun = true)

    private val facade = AuthenticationFacade(
        authenticationService,
        tokenProvider,
        verificationCodeStore,
        verificationSendLimiter,
        refreshTokenStore,
        rotatedTokenStore,
        accessTokenBlacklistStore,
        messageSender,
    )

    @Nested
    inner class RotateToken {

        private val request = RotateTokenRequest(
            memberId = 1L,
            accessToken = "old-access",
            refreshToken = "old-refresh",
        )

        @Test
        fun `유효한 리프레시 토큰이면 새 토큰을 발급하고 옛 토큰을 폐기하며 grace 기록을 남긴다`() {
            every { refreshTokenStore.getMemberId("old-refresh") } returns 1L
            every { authenticationService.getMember(1L) } returns memberFixture(id = 1L)
            every { tokenProvider.createAccessToken(1L) } returns "new-access"
            every { tokenProvider.createRefreshToken(1L) } returns "new-refresh"

            val response = facade.rotateToken(request)

            assertThat(response.memberId).isEqualTo(1L)
            assertThat(response.accessToken).isEqualTo("new-access")
            assertThat(response.refreshToken).isEqualTo("new-refresh")

            verify { accessTokenBlacklistStore.save(1L, "old-access") }
            verify { refreshTokenStore.save(1L, "new-refresh") }
            verify { rotatedTokenStore.save("old-refresh", 1L, "new-access", "new-refresh") }
            verify { refreshTokenStore.delete("old-refresh") }
        }

        @Test
        fun `활성 토큰이 없어도 grace 기록이 있으면 동일한 토큰을 멱등하게 반환한다`() {
            every { refreshTokenStore.getMemberId("old-refresh") } returns null
            every { rotatedTokenStore.get("old-refresh") } returns RotatedTokens(
                memberId = 1L,
                accessToken = "new-access",
                refreshToken = "new-refresh",
            )

            val response = facade.rotateToken(request)

            assertThat(response.accessToken).isEqualTo("new-access")
            assertThat(response.refreshToken).isEqualTo("new-refresh")

            // 멱등 재시도이므로 새 토큰을 다시 발급하거나 옛 토큰을 또 폐기하지 않는다.
            verify(exactly = 0) { tokenProvider.createAccessToken(any()) }
            verify(exactly = 0) { refreshTokenStore.delete(any()) }
        }

        @Test
        fun `활성 토큰도 없고 grace 기록도 없으면 UNAUTHORIZED_02 예외가 발생한다`() {
            every { refreshTokenStore.getMemberId("old-refresh") } returns null
            every { rotatedTokenStore.get("old-refresh") } returns null

            assertThatThrownBy { facade.rotateToken(request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_02)
                }
        }

        @Test
        fun `grace 기록의 소유자가 요청 회원과 다르면 UNAUTHORIZED_02 예외가 발생한다`() {
            every { refreshTokenStore.getMemberId("old-refresh") } returns null
            every { rotatedTokenStore.get("old-refresh") } returns RotatedTokens(
                memberId = 2L,
                accessToken = "new-access",
                refreshToken = "new-refresh",
            )

            assertThatThrownBy { facade.rotateToken(request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_02)
                }
        }

        @Test
        fun `토큰 소유자가 요청 회원과 다르면 UNAUTHORIZED_02 예외가 발생한다`() {
            every { refreshTokenStore.getMemberId("old-refresh") } returns 999L

            assertThatThrownBy { facade.rotateToken(request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_02)
                }

            verify(exactly = 0) { tokenProvider.createAccessToken(any()) }
        }
    }
}
