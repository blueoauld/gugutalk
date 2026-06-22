package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.repository.LikeRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.activityResultFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class LikeServiceTest {

    private val likeRepository = mockk<LikeRepository>(relaxUnitFun = true)
    private val likeService = LikeService(likeRepository)

    @Nested
    inner class Create {

        @Test
        fun `좋아요를 누른 적이 없으면 저장된다`() {
            every { likeRepository.existsByFromIdAndToId(1L, 2L) } returns false
            every { likeRepository.save(any()) } answers { firstArg() }

            likeService.create(memberId = 1L, targetId = 2L)

            verify(exactly = 1) { likeRepository.save(any()) }
        }

        @Test
        fun `이미 좋아요를 누른 대상이면 ACTIVITY_01 예외가 발생한다`() {
            every { likeRepository.existsByFromIdAndToId(1L, 2L) } returns true

            assertThatThrownBy { likeService.create(memberId = 1L, targetId = 2L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_01)
                }

            verify(exactly = 0) { likeRepository.save(any()) }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `삭제된 행이 있으면 정상 처리된다`() {
            every { likeRepository.deleteByFromIdAndToId(1L, 2L) } returns 1

            likeService.delete(memberId = 1L, targetId = 2L)

            verify(exactly = 1) { likeRepository.deleteByFromIdAndToId(1L, 2L) }
        }

        @Test
        fun `삭제된 행이 없으면 ACTIVITY_02 예외가 발생한다`() {
            every { likeRepository.deleteByFromIdAndToId(1L, 2L) } returns 0

            assertThatThrownBy { likeService.delete(memberId = 1L, targetId = 2L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_02)
                }
        }
    }

    @Nested
    inner class Gets {

        @Test
        fun `요청 size 보다 한 건 더 조회되면 hasNext 가 true 이고 마지막 한 건은 잘라낸다`() {
            val results = listOf(
                activityResultFixture(activityId = 3L, createdAt = Instant.parse("2026-01-03T00:00:00Z")),
                activityResultFixture(activityId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                activityResultFixture(activityId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                likeRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = 3)
            } returns results

            val response = likeService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }

        @Test
        fun `조회 결과가 size 이하면 hasNext 가 false 이고 전부 반환한다`() {
            val results = listOf(
                activityResultFixture(activityId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                activityResultFixture(activityId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            val sizeSlot = slot<Int>()
            every {
                likeRepository.findAllByCursor(
                    memberId = 1L,
                    cursorId = null,
                    cursorDateAt = null,
                    size = capture(sizeSlot)
                )
            } returns results

            val response = likeService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(sizeSlot.captured).isEqualTo(3) // 서비스가 size + 1 로 조회하는지 확인
            assertThat(response.hasNext).isFalse()
            assertThat(response.payload).hasSize(2)
        }
    }
}
