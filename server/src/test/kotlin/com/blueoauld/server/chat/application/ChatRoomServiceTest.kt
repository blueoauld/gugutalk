package com.blueoauld.server.chat.application

import com.blueoauld.server.activity.repository.BlockRepository
import com.blueoauld.server.chat.application.event.ChatRoomDeleteEvent
import com.blueoauld.server.chat.application.event.ChatRoomReadEvent
import com.blueoauld.server.chat.application.request.ChatRoomCreateRequest
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.fixture.*
import com.blueoauld.server.member.repository.MemberRepository
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
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.*

class ChatRoomServiceTest {

    private val chatRoomRepository = mockk<ChatRoomRepository>(relaxUnitFun = true)
    private val chatMessageRepository = mockk<ChatMessageRepository>(relaxUnitFun = true)
    private val memberRepository = mockk<MemberRepository>(relaxUnitFun = true)
    private val pointRepository = mockk<PointRepository>(relaxUnitFun = true)
    private val pointHistoryRepository = mockk<PointHistoryRepository>(relaxUnitFun = true)
    private val blockRepository = mockk<BlockRepository>(relaxUnitFun = true)
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxUnitFun = true)
    private val chatRoomService = ChatRoomService(
        chatRoomRepository,
        chatMessageRepository,
        memberRepository,
        pointRepository,
        pointHistoryRepository,
        blockRepository,
        applicationEventPublisher,
    )

    @Nested
    inner class Create {

        private val request = ChatRoomCreateRequest(content = "안녕하세요")

        @Test
        fun `차단 관계가 있으면 CHAT_05 예외가 발생한다`() {
            every { blockRepository.existsBlockBetween(1L, 2L) } returns true

            assertThatThrownBy { chatRoomService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_05)
                }
        }

        @Test
        fun `포인트 계정이 없으면 POINT_01 예외가 발생한다`() {
            every { blockRepository.existsBlockBetween(1L, 2L) } returns false
            every { pointRepository.findByMemberId(1L) } returns null

            assertThatThrownBy { chatRoomService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_01)
                }
        }

        @Test
        fun `잔액이 쪽지 전송 비용보다 적으면 POINT_03 예외가 발생한다`() {
            every { blockRepository.existsBlockBetween(1L, 2L) } returns false
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = PointSource.MESSAGE_SEND.point - 1)

            assertThatThrownBy { chatRoomService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.POINT_03)
                }
        }

        @Test
        fun `상대가 쪽지 수신을 거부했으면 CHAT_04 예외가 발생한다`() {
            every { blockRepository.existsBlockBetween(1L, 2L) } returns false
            every { pointRepository.findByMemberId(1L) } returns pointFixture(balance = 100L)
            every { memberRepository.findById(2L) } returns Optional.of(memberFixture(id = 2L).also { it.toggleChat() })

            assertThatThrownBy { chatRoomService.create(memberId = 1L, targetId = 2L, request = request) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_04)
                }
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `존재하지 않는 채팅방이면 CHAT_03 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.empty()

            assertThatThrownBy { chatRoomService.delete(memberId = 1L, chatRoomId = 10L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_03)
                }
        }

        @Test
        fun `채팅방 참여자가 아니면 CHAT_02 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.of(
                chatRoomFixture(
                    id = 10L,
                    member1Id = 2L,
                    member2Id = 3L
                )
            )

            assertThatThrownBy { chatRoomService.delete(memberId = 1L, chatRoomId = 10L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_02)
                }

            verify(exactly = 0) { chatRoomRepository.delete(any()) }
        }

        @Test
        fun `참여자면 채팅방을 삭제하고 삭제 이벤트를 발행한다`() {
            val chatRoom = chatRoomFixture(id = 10L, member1Id = 1L, member2Id = 2L)
            every { chatRoomRepository.findById(10L) } returns Optional.of(chatRoom)

            chatRoomService.delete(memberId = 1L, chatRoomId = 10L)

            verify(exactly = 1) { chatRoomRepository.delete(chatRoom) }
            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(
                    ChatRoomDeleteEvent(chatRoomId = 10L, targetId = 2L, memberId = 1L)
                )
            }
        }
    }

    @Nested
    inner class Read {

        @Test
        fun `존재하지 않는 채팅방이면 CHAT_03 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.empty()

            assertThatThrownBy { chatRoomService.read(memberId = 1L, chatRoomId = 10L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_03)
                }
        }

        @Test
        fun `마지막 메세지가 없으면 아무 것도 하지 않고 종료한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.of(chatRoomFixture(id = 10L))
            every { chatMessageRepository.findLastMessageId(10L) } returns null

            chatRoomService.read(memberId = 1L, chatRoomId = 10L)

            verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
        }

        @Test
        fun `마지막 메세지가 있으면 읽음 처리하고 이벤트를 발행한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.of(
                chatRoomFixture(
                    id = 10L,
                    member1Id = 1L,
                    member2Id = 2L
                )
            )
            every { chatMessageRepository.findLastMessageId(10L) } returns 5L

            chatRoomService.read(memberId = 1L, chatRoomId = 10L)

            verify(exactly = 1) {
                applicationEventPublisher.publishEvent(ChatRoomReadEvent(chatRoomId = 10L, memberId = 1L))
            }
        }
    }

    @Nested
    inner class Gets {

        @Test
        fun `요청 size 보다 한 건 더 조회되면 hasNext 가 true 이고 마지막 한 건은 잘라낸다`() {
            val results = listOf(
                chatRoomResultFixture(chatRoomId = 3L, lastMessageAt = Instant.parse("2026-01-03T00:00:00Z")),
                chatRoomResultFixture(chatRoomId = 2L, lastMessageAt = Instant.parse("2026-01-02T00:00:00Z")),
                chatRoomResultFixture(chatRoomId = 1L, lastMessageAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                chatRoomRepository.findAllByCursor(
                    memberId = 1L,
                    status = "ALL",
                    cursorId = null,
                    cursorDateAt = null,
                    size = 3
                )
            } returns results

            val response =
                chatRoomService.gets(memberId = 1L, status = "ALL", cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }
    }

    @Nested
    inner class Search {

        @Test
        fun `닉네임이 2자 미만이면 SEARCH_01 예외가 발생한다`() {
            assertThatThrownBy {
                chatRoomService.search(memberId = 1L, nickname = "a", cursorId = null, cursorDateAt = null, size = 2)
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.SEARCH_01)
            }

            verify(exactly = 0) { chatRoomRepository.findAllByNickname(any(), any(), any(), any(), any()) }
        }

        @Test
        fun `닉네임으로 커서 조회하고 페이징한다`() {
            val results = listOf(
                chatRoomSearchResultFixture(chatRoomId = 2L, lastMessageAt = Instant.parse("2026-01-02T00:00:00Z")),
                chatRoomSearchResultFixture(chatRoomId = 1L, lastMessageAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                chatRoomRepository.findAllByNickname(
                    memberId = 1L,
                    nickname = "철수",
                    cursorId = null,
                    cursorDateAt = null,
                    size = 3
                )
            } returns results

            val response =
                chatRoomService.search(memberId = 1L, nickname = "철수", cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isFalse()
            assertThat(response.payload).hasSize(2)
        }
    }
}
