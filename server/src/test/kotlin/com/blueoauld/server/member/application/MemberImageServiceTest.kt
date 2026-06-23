package com.blueoauld.server.member.application

import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.r2.application.R2Provider
import com.blueoauld.server.r2.application.request.UploadUrlRequest
import com.blueoauld.server.r2.application.request.UploadUrlRequests
import com.blueoauld.server.r2.application.response.UploadUrlResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration

class MemberImageServiceTest {

    private val r2Provider = mockk<R2Provider>()
    private val memberImageService = MemberImageService(r2Provider)

    private fun req(contentType: String) = UploadUrlRequest(contentType = contentType, contentLength = 1024)

    @Nested
    inner class CreatePublicUploadUrls {

        @Test
        fun `요청한 개수만큼 공개 temporary 경로의 업로드 URL을 발급한다`() {
            val keys = mutableListOf<String>()
            every { r2Provider.createUploadUrl(capture(keys), any(), any(), any()) } returns
                UploadUrlResponse(url = "https://signed", key = "k")

            val response = memberImageService.createPublicUploadUrls(
                memberId = 1L,
                requests = UploadUrlRequests(listOf(req("image/jpeg"), req("image/png"))),
            )

            assertThat(response.urls).hasSize(2)
            assertThat(keys).hasSize(2)
            assertThat(keys).allSatisfy { assertThat(it).startsWith("member/public/temporary/1/") }
            assertThat(keys[0]).endsWith(".jpg")
            assertThat(keys[1]).endsWith(".png")
        }

        @Test
        fun `만료 시간 5분으로 업로드 URL을 발급한다`() {
            val expiry = slot<Duration>()
            every { r2Provider.createUploadUrl(any(), any(), any(), capture(expiry)) } returns
                UploadUrlResponse(url = "https://signed", key = "k")

            memberImageService.createPublicUploadUrls(1L, UploadUrlRequests(listOf(req("image/jpeg"))))

            assertThat(expiry.captured).isEqualTo(Duration.ofMinutes(5))
        }

        @Test
        fun `지원하지 않는 컨텐츠 타입이면 FILE_01 예외가 발생한다`() {
            assertThatThrownBy {
                memberImageService.createPublicUploadUrls(1L, UploadUrlRequests(listOf(req("application/zip"))))
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.FILE_01)
            }
        }
    }

    @Nested
    inner class CreatePrivateUploadUrls {

        @Test
        fun `비공개 temporary 경로의 업로드 URL을 발급한다`() {
            val keys = mutableListOf<String>()
            every { r2Provider.createUploadUrl(capture(keys), any(), any(), any()) } returns
                UploadUrlResponse(url = "https://signed", key = "k")

            memberImageService.createPrivateUploadUrls(1L, UploadUrlRequests(listOf(req("image/jpeg"))))

            assertThat(keys).allSatisfy { assertThat(it).startsWith("member/private/temporary/1/") }
        }
    }
}
