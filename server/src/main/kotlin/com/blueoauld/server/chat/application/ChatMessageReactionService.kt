package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.entity.ChatMessageReaction
import com.blueoauld.server.chat.entity.type.ReactionType
import com.blueoauld.server.chat.repository.ChatMessageReactionRepository
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import com.blueoauld.server.common.exception.CustomException
import com.blueoauld.server.common.exception.type.ErrorCode.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageReactionService(

    private val chatMessageReactionRepository: ChatMessageReactionRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {

    @Transactional
    fun react(
        memberId: Long,
        chatRoomId: Long,
        chatMessageId: Long,
        type: ReactionType,
    ) {
        val chatRoom = chatRoomRepository.findByIdOrNull(chatRoomId) ?: throw CustomException(CHAT_03)
        if (chatRoom.member1Id != memberId && chatRoom.member2Id != memberId) {
            throw CustomException(CHAT_02)
        }

        if (!chatMessageRepository.existsById(chatMessageId)) {
            throw CustomException(CHAT_MESSAGE_01)
        }

        val chatMessageReaction = chatMessageReactionRepository.findByChatMessageIdAndMemberId(chatMessageId, memberId)

        when {
            chatMessageReaction == null -> chatMessageReactionRepository.save(
                ChatMessageReaction(
                    chatMessageId = chatMessageId,
                    memberId = memberId,
                    type = type
                )
            )

            chatMessageReaction.type == type -> chatMessageReactionRepository.delete(chatMessageReaction)

            else -> chatMessageReaction.changeType(type)
        }
    }
}