package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatMessageMedia
import com.blueoauld.server.chat.repository.result.MediaKeyView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ChatMessageMediaRepository : JpaRepository<ChatMessageMedia, Long> {

    fun findByChatMessageId(chatMessageId: Long): ChatMessageMedia?

    @Query(
        value = """
            SELECT cmm.key AS key, cmm.thumbnailKey AS thumbnailKey
            FROM ChatMessageMedia cmm
            WHERE cmm.chatMessageId IN (
                SELECT cm.id 
                FROM ChatMessage cm 
                WHERE cm.chatRoomId IN :chatRoomIds
            )
        """
    )
    fun findKeysByChatRoomIds(@Param("chatRoomIds") chatRoomIds: List<Long>): List<MediaKeyView>

    @Modifying
    @Query(
        value = """
            DELETE FROM ChatMessageMedia cmm
            WHERE cmm.chatMessageId IN (
                SELECT cm.id 
                FROM ChatMessage cm 
                WHERE cm.chatRoomId IN :chatRoomIds
            )
        """
    )
    fun deleteByChatRoomIds(@Param("chatRoomIds") chatRoomIds: List<Long>): Int
}