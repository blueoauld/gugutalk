package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.event.ChatMessageSendEvent
import com.blueoauld.server.chat.application.event.ChatRoomSendEvent
import com.blueoauld.server.chat.application.request.ChatMessageSendRequest
import com.blueoauld.server.chat.application.response.ChatMessageRowResponse
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.dto.response.CursorResponse
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.CHAT_02
import com.blueoauld.server.common.exception.type.ErrorCode.CHAT_03
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatMessageService(

    private val chatMessageRepository: ChatMessageRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional(readOnly = true)
    fun gets(
        memberId: Long,
        chatRoomId: Long,
        cursorId: Long?,
        cursorDateAt: Instant?,
        size: Int
    ): CursorResponse<ChatMessageRowResponse> {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val result = chatMessageRepository.findAllByCursor(
            memberId = memberId,
            chatRoomId = chatRoomId,
            cursorId = cursorId,
            cursorDateAt = cursorDateAt,
            size = size + 1
        ).map {
            ChatMessageRowResponse.from(it)
        }

        val hasNext = result.size > size
        val items = if (hasNext) result.dropLast(1) else result
        val last = items.lastOrNull()

        return CursorResponse(
            payload = items,
            nextId = last?.chatMessageId,
            nextDateAt = last?.createdAt,
            hasNext = hasNext
        )
    }

    @Transactional
    fun send(memberId: Long, chatRoomId: Long, request: ChatMessageSendRequest) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        val chatMessage = ChatMessage(
            chatRoomId = chatRoomId,
            senderId = memberId,
            type = MessageType.TEXT,
            content = request.content
        )
        chatMessageRepository.save(chatMessage)
        chatRoom.onMessageSent(memberId, chatMessage)

        // 이벤트
        applicationEventPublisher.publishEvent(
            ChatMessageSendEvent(
                chatMessageId = chatMessage.id,
                chatRoomId = chatRoomId,
                senderId = memberId,
                content = request.content,
                type = MessageType.TEXT,
                createdAt = chatMessage.createdAt
            )
        )

        applicationEventPublisher.publishEvent(
            ChatRoomSendEvent(
                chatRoomId = chatRoomId,
                memberId = chatRoom.getOtherMemberId(memberId),
                lastMessagePreview = request.content.take(100),
                lastMessageAt = chatMessage.createdAt,
            )
        )
    }
}