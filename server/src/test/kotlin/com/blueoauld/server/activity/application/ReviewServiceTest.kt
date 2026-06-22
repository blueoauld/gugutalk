package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.application.request.ReviewCreateRequest
import com.blueoauld.server.activity.repository.ReviewRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.pointFixture
import com.blueoauld.server.fixture.reviewFixture
import com.blueoauld.server.fixture.reviewResultFixture
import com.blueoauld.server.point.entity.type.PointSource
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

class ReviewServiceTest {

    private val reviewRepository = mockk<ReviewRepository>(relaxUnitFun = true)
    private val pointRepository = mockk<PointRepository>(relaxUnitFun = true)
    private val pointHistoryRepository = mockk<PointHistoryRepository>(relaxUnitFun = true)
    private val reviewService = ReviewService(reviewRepository, pointRepository, pointHistoryRepository)

    private val request = ReviewCreateRequest(content = "좋은 사람이에요")

    @Nested
    inner class Create {

        @Test
        fun `자기 자신에게 리뷰를 작성하면 ACTIVITY_09 예외가 발생한다`() {
            assertThatThrownBy { reviewService.create(memberId = 1L, targetId = 1L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_09)
                }

            verify(exactly = 0) { reviewRepository.save(any()) }
        }

        @Test
        fun `포인트 계정이 없으면 POINT_01 예외가 발생한다`() {
            every { pointRepository.findByMemberId(1L) } returns null

            assertThatThrownBy { reviewService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_01)
                }
        }

        @Test
        fun `잔액이 리뷰 작성 비용보다 적으면 POINT_03 예외가 발생한다`() {
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = PointSource.REVIEW_WRITE.point - 1)

            assertThatThrownBy { reviewService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_03)
                }

            verify(exactly = 0) { reviewRepository.save(any()) }
        }

        @Test
        fun `정상 작성되면 리뷰와 포인트 이력을 저장하고 잔액을 차감한다`() {
            val point = pointFixture(balance = 100L)
            every { pointRepository.findByMemberId(1L) } returns point
            every { reviewRepository.save(any()) } answers { firstArg() }
            every { pointHistoryRepository.save(any()) } answers { firstArg() }

            val response = reviewService.create(memberId = 1L, targetId = 2L, request = request)

            assertThat(response.fromId).isEqualTo(1L)
            assertThat(response.toId).isEqualTo(2L)
            assertThat(response.content).isEqualTo("좋은 사람이에요")
            assertThat(point.balance).isEqualTo(100L - PointSource.REVIEW_WRITE.point)
            verify(exactly = 1) { reviewRepository.save(any()) }
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `존재하지 않는 리뷰면 ACTIVITY_10 예외가 발생한다`() {
            every { reviewRepository.findById(99L) } returns Optional.empty()

            assertThatThrownBy { reviewService.delete(memberId = 1L, reviewId = 99L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_10)
                }
        }

        @Test
        fun `본인이 받은 리뷰가 아니면 ACTIVITY_11 예외가 발생한다`() {
            // 리뷰 수신자(toId)는 9, 삭제 요청자는 1
            every { reviewRepository.findById(1L) } returns Optional.of(reviewFixture(id = 1L, toId = 9L))

            assertThatThrownBy { reviewService.delete(memberId = 1L, reviewId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_11)
                }

            verify(exactly = 0) { reviewRepository.delete(any()) }
        }

        @Test
        fun `잔액이 리뷰 삭제 비용보다 적으면 POINT_03 예외가 발생한다`() {
            every { reviewRepository.findById(1L) } returns Optional.of(reviewFixture(id = 1L, toId = 1L))
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = PointSource.REVIEW_DELETE.point - 1)

            assertThatThrownBy { reviewService.delete(memberId = 1L, reviewId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_03)
                }

            verify(exactly = 0) { reviewRepository.delete(any()) }
        }

        @Test
        fun `정상 삭제되면 리뷰를 삭제하고 포인트 이력 저장 후 잔액을 차감한다`() {
            val review = reviewFixture(id = 1L, toId = 1L)
            val point = pointFixture(balance = 100L)
            every { reviewRepository.findById(1L) } returns Optional.of(review)
            every { pointRepository.findByMemberId(1L) } returns point
            every { pointHistoryRepository.save(any()) } answers { firstArg() }

            reviewService.delete(memberId = 1L, reviewId = 1L)

            assertThat(point.balance).isEqualTo(100L - PointSource.REVIEW_DELETE.point)
            verify(exactly = 1) { reviewRepository.delete(review) }
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    inner class Gets {

        @Test
        fun `요청 size 보다 한 건 더 조회되면 hasNext 가 true 이고 마지막 한 건은 잘라낸다`() {
            val results = listOf(
                reviewResultFixture(reviewId = 3L, createdAt = Instant.parse("2026-01-03T00:00:00Z")),
                reviewResultFixture(reviewId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                reviewResultFixture(reviewId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                reviewRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = 3)
            } returns results

            val response = reviewService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }
    }
}
