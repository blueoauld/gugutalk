package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.application.request.ChatRoomCreateRequest
import com.blueoauld.server.chat.entity.ChatMessage
import com.blueoauld.server.chat.entity.ChatRoom
import com.blueoauld.server.chat.entity.type.MessageType
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import com.blueoauld.server.member.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatRoomService(

    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val memberRepository: MemberRepository,
) {

    @Transactional
    fun create(memberId: Long, targetId: Long, request: ChatRoomCreateRequest) {
        if (!memberRepository.existsById(targetId)) {
            throw CustomException(MEMBER_01)
        }

        val room = chatRoomRepository.findByMember1IdAndMember2Id(
            minOf(memberId, targetId),
            maxOf(memberId, targetId)
        ) ?: chatRoomRepository.save(ChatRoom.create(memberId, targetId))

        val message = chatMessageRepository.save(
            ChatMessage(
                chatRoomId = room.id,
                senderId = memberId,
                type = MessageType.TEXT,
                content = request.content,
            )
        )

        room.onMessageSent(memberId, message)
    }

    @Transactional
    fun delete(memberId: Long, chatRoomId: Long) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)

        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        chatRoomRepository.delete(chatRoom)
    }
}