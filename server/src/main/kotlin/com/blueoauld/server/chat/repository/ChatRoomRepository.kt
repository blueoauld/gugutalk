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

    /**
     * 참여자(member1·member2) 중 한 명이라도 더 이상 활성 회원이 아닌데도 살아있는 채팅방을 소프트 삭제한다.
     *
     * 탈퇴 처리([com.blueoauld.server.authentication.application.AuthenticationService.deleteMember])는
     * 탈퇴 시점 스냅샷으로 방을 지우므로, 그 직후 상대가 방을 만들거나 갱신하는 경쟁 상황에서 일부 방이
     * `deleted_at IS NULL` 로 남을 수 있다. 이런 고아 방을 주기적으로 회수하기 위한 보정 쿼리.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        value = """
            UPDATE chat_room
            SET deleted_at = :now
            WHERE deleted_at IS NULL
              AND (
                NOT EXISTS (SELECT 1 FROM member m WHERE m.id = chat_room.member1_id AND m.deleted_at IS NULL)
                OR NOT EXISTS (SELECT 1 FROM member m WHERE m.id = chat_room.member2_id AND m.deleted_at IS NULL)
              )
        """,
        nativeQuery = true,
    )
    fun softDeleteOrphans(@Param("now") now: Instant): Int
}