package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.repository.ChatMessageMediaRepository
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatCleanupService(

    private val chatRoomRepository: ChatRoomRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatMessageMediaRepository: ChatMessageMediaRepository,
) {

    @Transactional
    fun deleteBatch(ids: List<Long>): List<String> {
        if (ids.isEmpty()) {
            return emptyList()
        }

        val imageKeys = chatMessageMediaRepository.findKeysByChatRoomIds(ids).flatMap {
            listOfNotNull(it.key, it.thumbnailKey)
        }

        chatMessageMediaRepository.deleteByChatRoomIds(ids)
        chatMessageRepository.deleteByChatRoomIdIn(ids)
        chatRoomRepository.hardDeleteByIds(ids)

        return imageKeys
    }
}