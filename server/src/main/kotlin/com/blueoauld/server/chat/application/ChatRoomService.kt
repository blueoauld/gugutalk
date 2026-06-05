package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.*
import com.blueoauld.server.chat.application.request.ChatRoomCreateRequest
import com.blueoauld.server.chat.application.response.ChatRoomRowResponse
import com.blueoauld.server.chat.application.response.ChatRoomSearchRowResponse
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.member.repository.MemberRepository
import com.blueoauld.server.point.entity.PointHistory
import com.blueoauld.server.point.entity.type.PointSource
import com.blueoauld.server.point.repository.PointHistoryRepository
import com.blueoauld.server.point.repository.PointRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatRoomService(

    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val memberRepository: MemberRepository,
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun create(memberId: Long, targetId: Long, request: ChatRoomCreateRequest) {
        val member = memberRepository.findByIdOrNull(memberId) ?: throw CustomException(MEMBER_01)
        val target = memberRepository.findByIdOrNull(targetId) ?: throw CustomException(MEMBER_01)

        val point = pointRepository.findByMemberId(memberId) ?: throw CustomException(POINT_01)
        if (point.balance < PointSource.MESSAGE_SEND.point) {
            throw CustomException(POINT_03)
        }

        val chatRoom = chatRoomRepository.findByMember1IdAndMember2Id(
            minOf(memberId, targetId),
            maxOf(memberId, targetId)
        ) ?: chatRoomRepository.save(ChatRoom.create(memberId, targetId))

        val chatMessage = chatMessageRepository.save(
            ChatMessage(
                chatRoomId = chatRoom.id,
                senderId = memberId,
                type = MessageType.TEXT,
                content = request.content,
            )
        )

        chatRoom.onMessageSent(memberId, chatMessage)

        val pointHistory = PointHistory(
            pointId = point.id,
            source = PointSource.MESSAGE_SEND,
            balanceSnapshot = point.balance,
        )
        pointHistoryRepository.save(pointHistory)
        point.use(PointSource.MESSAGE_SEND.point)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatMessageSendEvent(
                chatMessageId = chatMessage.id,
                chatRoomId = chatRoom.id,
                senderId = memberId,
                content = request.content,
                type = MessageType.TEXT,
                createdAt = chatMessage.createdAt
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoom.id,
                targetId = targetId,
                memberId = memberId,
                senderId = memberId,
                nickname = member.nickname,
                profileUrl = member.profileUrl,
                lastMessagePreview = request.content.take(100),
                lastMessageAt = chatMessage.createdAt,
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomUpsertEvent(
                chatRoomId = chatRoom.id,
                targetId = memberId,
                memberId = targetId,
                senderId = memberId,
                nickname = target.nickname,
                profileUrl = target.profileUrl,
                lastMessagePreview = request.content.take(100),
                lastMessageAt = chatMessage.createdAt,
            )
        )

        // 알림
        applicationEventPublisher.publishEvent(
            ChatMessagePushEvent(
                chatRoomId = chatRoom.id,
                targetId = targetId,
                senderId = memberId,
                nickname = member.nickname,
                profileUrl = member.profileUrl,
                lastMessagePreview = request.content.take(100),
            )
        )
    }

    @Transactional
    fun delete(memberId: Long, chatRoomId: Long) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        chatRoomRepository.delete(chatRoom)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatRoomDeleteEvent(
                chatRoomId = chatRoomId,
                targetId = chatRoom.getOtherMemberId(memberId)
            )
        )
    }

    @Transactional
    fun read(memberId: Long, chatRoomId: Long) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)
        val lastMessageId = chatMessageRepository.findLastMessageId(chatRoomId) ?: return

        chatRoom.markAsRead(memberId, lastMessageId)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatRoomReadEvent(
                chatRoomId = chatRoomId,
                memberId = memberId
            )
        )
    }

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        status: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ChatRoomRowResponse> {
        val result = chatRoomRepository.findAllByCursor(
            memberId = memberId,
            status = status,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ChatRoomRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.chatRoomId,
            nextDateAt = last?.lastMessageAt,
            hasNext = hasNext
        )
    }

    @Transactional(readOnly = true)
    fun search(
        memberId: Long,
        nickname: String,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ChatRoomSearchRowResponse> {
        if (nickname.length < 2) {
            throw CustomException(SEARCH_01)
        }

        val result = chatRoomRepository.findAllByNickname(
            memberId = memberId,
            nickname = nickname,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ChatRoomSearchRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.chatRoomId,
            nextDateAt = last?.lastMessageAt,
            hasNext = hasNext
        )
    }
}