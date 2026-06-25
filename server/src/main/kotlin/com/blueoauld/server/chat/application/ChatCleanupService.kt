package com.blueoauld.server.chat.application

import com.blueoauld.server.chat.repository.ChatMessageMediaRepository
import com.blueoauld.server.chat.repository.ChatMessageRepository
import com.blueoauld.server.chat.repository.ChatRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

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

    /**
     * 탈퇴 처리 누락(경쟁 상황)으로 살아남은 고아 채팅방을 소프트 삭제한다.
     * 실제 메시지/이미지/행 삭제는 90일 후 [deleteBatch] 가 처리한다.
     */
    @Transactional
    fun reconcileOrphans(now: Instant): Int {
        return chatRoomRepository.softDeleteOrphans(now)
    }
}