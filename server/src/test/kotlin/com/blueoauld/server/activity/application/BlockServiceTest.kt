package com.blueoauld.server.activity.application

import com.blueoauld.server.activity.repository.BlockRepository
import com.blueoauld.server.chat.application.event.ChatRoomDeleteEvent
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.activityResultFixture
import com.blueoauld.server.fixture.chatRoomFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

class BlockServiceTest {

    private val blockRepository = mockk<BlockRepository>(relaxUnitFun = true)
    private val chatRoomRepository = mockk<ChatRoomRepository>(relaxUnitFun = true)
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxUnitFun = true)
    private val blockService = BlockService(blockRepository, chatRoomRepository, applicationEventPublisher)

    @Nested
    inner class Create {

        @Test
        fun `차단한 적이 없으면 저장된다`() {
            every { blockRepository.existsByFromIdAndToId(1L, 2L) } returns false
            every { blockRepository.save(any()) } answers { firstArg() }
            every { chatRoomRepository.findByMember1IdAndMember2Id(1L, 2L) } returns null

            blockService.create(memberId = 1L, targetId = 2L)

            verify(exactly = 1) { blockRepository.save(any()) }
        }

        @Test
        fun `이미 차단한 대상이면 ACTIVITY_05 예외가 발생한다`() {
            every { blockRepository.existsByFromIdAndToId(1L, 2L) } returns true

            assertThatThrownBy { blockService.create(memberId = 1L, targetId = 2L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_05)
                }

            verify(exactly = 0) { blockRepository.save(any()) }
        }

        @Test
        fun `차단 시 두 회원의 채팅방이 있으면 삭제하고 삭제 이벤트를 발행한다`() {
            // memberId, targetId 를 정렬한 (min, max) 로 채팅방을 조회한다
            val chatRoom = chatRoomFixture(id = 10L, member1Id = 1L, member2Id = 2L)
            every { blockRepository.existsByFromIdAndToId(1L, 2L) } returns false
            every { blockRepository.save(any()) } answers { firstArg() }
            every { chatRoomRepository.findByMember1IdAndMember2Id(1L, 2L) } returns chatRoom

            blockService.create(memberId = 1L, targetId = 2L)

            verify(exactly = 1) { chatRoomRepository.delete(chatRoom) }
            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(
                    ChatRoomDeleteEvent(chatRoomId = 10L, targetId = 2L, memberId = 1L)
                )
            }
        }

        @Test
        fun `차단 시 채팅방이 없으면 삭제나 이벤트 발행을 하지 않는다`() {
            every { blockRepository.existsByFromIdAndToId(1L, 2L) } returns false
            every { blockRepository.save(any()) } answers { firstArg() }
            every { chatRoomRepository.findByMember1IdAndMember2Id(1L, 2L) } returns null

            blockService.create(memberId = 1L, targetId = 2L)

            verify(exactly = 0) { chatRoomRepository.delete(any()) }
            verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
        }

        @Test
        fun `targetId 가 memberId 보다 작아도 정렬된 순서로 채팅방을 조회한다`() {
            every { blockRepository.existsByFromIdAndToId(5L, 2L) } returns false
            every { blockRepository.save(any()) } answers { firstArg() }
            every { chatRoomRepository.findByMember1IdAndMember2Id(2L, 5L) } returns null

            blockService.create(memberId = 5L, targetId = 2L)

            verify(exactly = 1) { chatRoomRepository.findByMember1IdAndMember2Id(2L, 5L) }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `차단을 해제하면 정상 처리된다`() {
            every { blockRepository.deleteByFromIdAndToId(1L, 2L) } returns 1

            blockService.delete(memberId = 1L, targetId = 2L)

            verify(exactly = 1) { blockRepository.deleteByFromIdAndToId(1L, 2L) }
        }

        @Test
        fun `차단한 적이 없으면 ACTIVITY_06 예외가 발생한다`() {
            every { blockRepository.deleteByFromIdAndToId(1L, 2L) } returns 0

            assertThatThrownBy { blockService.delete(memberId = 1L, targetId = 2L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.ACTIVITY_06)
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
            val sizeSlot = slot<Int>()
            every {
                blockRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = capture(sizeSlot))
            } returns results

            val response = blockService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(sizeSlot.captured).isEqualTo(3)
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
            every {
                blockRepository.findAllByCursor(memberId = 1L, cursorId = null, cursorDateAt = null, size = 3)
            } returns results

            val response = blockService.gets(memberId = 1L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isFalse()
            assertThat(response.payload).hasSize(2)
        }
    }
}
