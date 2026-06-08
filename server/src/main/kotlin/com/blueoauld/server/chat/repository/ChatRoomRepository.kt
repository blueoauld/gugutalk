package com.blueoauld.server.chat.repository

import com.blueoauld.server.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ChatRoomRepository : JpaRepository<ChatRoom, Long>, ChatRoomCustomRepository {

    fun findByMember1IdAndMember2Id(member1Id: Long, member2Id: Long): ChatRoom?

    fun findAllByMember1IdOrMember2Id(member1Id: Long, member2Id: Long): List<ChatRoom>

    @Query(
        value = """
            SELECT id
            FROM chat_room
            WHERE deleted_at < :threshold
            ORDER BY id
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findAllByDeletedIds(
        @Param("threshold") threshold: Instant,
        @Param("limit") limit: Int,
    ): List<Long>

    @Modifying
    @Query(value = "DELETE FROM chat_room WHERE id IN (:ids)", nativeQuery = true)
    fun hardDeleteByIds(@Param("ids") ids: List<Long>): Int
}