package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface MemberRepository : JpaRepository<Member, Long>, MemberCustomRepository {

    fun existsByPhone(phone: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByPhone(phone: String): Member?

    fun findByNickname(nickname: String): Member?

    @Query(
        value = """
            SELECT id
            FROM member
            WHERE deleted_at IS NOT NULL AND deleted_at < :threshold
            ORDER BY id
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findDeletedMemberIds(
        @Param("threshold") threshold: Instant,
        @Param("limit") limit: Int,
    ): List<Long>

    @Modifying
    @Query(value = "DELETE FROM member WHERE id IN (:ids)", nativeQuery = true)
    fun hardDeleteByIds(@Param("ids") ids: List<Long>): Int
}