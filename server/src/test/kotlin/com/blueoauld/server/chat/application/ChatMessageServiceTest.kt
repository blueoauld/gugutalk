package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.request.ChatMessageSendRequest
import com.blueoauld.server.chat.repository.ChatMessageMediaRepository
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode
import com.blueoauld.server.common.properties.R2Properties
import com.blueoauld.server.fixture.chatMessageResultFixture
import com.blueoauld.server.fixture.chatRoomFixture
import com.blueoauld.server.fixture.memberFixture
import com.blueoauld.server.r2.application.R2Provider
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

class ChatMessageServiceTest {

    private val chatMessageRepository = mockk<ChatMessageRepository>(relaxUnitFun = true)
    private val chatRoomRepository = mockk<ChatRoomRepository>(relaxUnitFun = true)
    private val chatMessageMediaRepository = mockk<ChatMessageMediaRepository>(relaxUnitFun = true)
    private val memberRepository = mockk<com.blueoauld.server.member.repository.MemberRepository>(relaxUnitFun = true)
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxUnitFun = true)
    private val r2Provider = mockk<R2Provider>(relaxUnitFun = true)
    private val r2Properties = mockk<R2Properties>(relaxUnitFun = true)
    private val chatMessageService = ChatMessageService(
        chatMessageRepository,
        chatRoomRepository,
        chatMessageMediaRepository,
        memberRepository,
        applicationEventPublisher,
        r2Provider,
        r2Properties,
    )

    @Nested
    inner class Gets {

        @Test
        fun `존재하지 않는 채팅방이면 CHAT_03 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.empty()

            assertThatThrownBy {
                chatMessageService.gets(memberId = 1L, chatRoomId = 10L, cursorId = null, cursorDateAt = null, size = 2)
            }.isInstanceOfSatisfying(CustomException::class.java) {
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

            assertThatThrownBy {
                chatMessageService.gets(memberId = 1L, chatRoomId = 10L, cursorId = null, cursorDateAt = null, size = 2)
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_02)
            }
        }

        @Test
        fun `참여자면 메세지를 커서 조회하고 페이징한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.of(
                chatRoomFixture(
                    id = 10L,
                    member1Id = 1L,
                    member2Id = 2L
                )
            )
            val results = listOf(
                chatMessageResultFixture(chatMessageId = 3L, createdAt = Instant.parse("2026-01-03T00:00:00Z")),
                chatMessageResultFixture(chatMessageId = 2L, createdAt = Instant.parse("2026-01-02T00:00:00Z")),
                chatMessageResultFixture(chatMessageId = 1L, createdAt = Instant.parse("2026-01-01T00:00:00Z")),
            )
            every {
                chatMessageRepository.findAllByCursor(
                    memberId = 1L,
                    chatRoomId = 10L,
                    cursorId = null,
                    cursorDateAt = null,
                    size = 3
                )
            } returns results

            val response =
                chatMessageService.gets(memberId = 1L, chatRoomId = 10L, cursorId = null, cursorDateAt = null, size = 2)

            assertThat(response.hasNext).isTrue()
            assertThat(response.payload).hasSize(2)
            assertThat(response.nextId).isEqualTo(2L)
            assertThat(response.nextDateAt).isEqualTo(Instant.parse("2026-01-02T00:00:00Z"))
        }
    }

    @Nested
    inner class GetVideo {

        @Test
        fun `존재하지 않는 미디어면 CHAT_MESSAGE_01 예외가 발생한다`() {
            every { chatMessageMediaRepository.findByChatMessageId(1L) } returns null

            assertThatThrownBy { chatMessageService.getVideo(chatMessageId = 1L) }
                .isInstanceOfSatisfying(CustomException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_MESSAGE_01)
                }
        }
    }

    @Nested
    inner class Send {

        @Test
        fun `참여자면 메세지를 저장하고 전송_채팅방_알림 이벤트를 발행한다`() {
            every { chatRoomRepository.findById(10L) } returns
                    Optional.of(chatRoomFixture(id = 10L, member1Id = 1L, member2Id = 2L))
            every { memberRepository.findById(1L) } returns Optional.of(memberFixture(id = 1L, nickname = "나"))
            every { memberRepository.findById(2L) } returns Optional.of(memberFixture(id = 2L, nickname = "상대"))
            every { chatMessageRepository.save(any()) } answers { firstArg() }

            chatMessageService.send(memberId = 1L, chatRoomId = 10L, request = ChatMessageSendRequest(content = "안녕"))

            verify(exactly = 1) { chatMessageRepository.save(any()) }
            // 전송 1 + 채팅방 upsert 2(양쪽) + 푸시 1 = 4건
            verify(exactly = 4) { applicationEventPublisher.publishEvent(any<Any>()) }
        }

        @Test
        fun `존재하지 않는 채팅방이면 CHAT_03 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns Optional.empty()

            assertThatThrownBy {
                chatMessageService.send(
                    memberId = 1L,
                    chatRoomId = 10L,
                    request = ChatMessageSendRequest(content = "안녕")
                )
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_03)
            }

            verify(exactly = 0) { chatMessageRepository.save(any()) }
        }

        @Test
        fun `참여자가 아니면 CHAT_02 예외가 발생한다`() {
            every { chatRoomRepository.findById(10L) } returns
                    Optional.of(chatRoomFixture(id = 10L, member1Id = 2L, member2Id = 3L))

            assertThatThrownBy {
                chatMessageService.send(
                    memberId = 1L,
                    chatRoomId = 10L,
                    request = ChatMessageSendRequest(content = "안녕")
                )
            }.isInstanceOfSatisfying(CustomException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.CHAT_02)
            }
        }
    }
}
