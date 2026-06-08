package com.blueoauld.server.ban.repository

import com.blueoauld.server.ban.entity.Ban
import com.blueoauld.server.ban.entity.type.BanType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface BanRepository : JpaRepository<Ban, Long> {

    fun existsByTypeAndTarget(type: BanType, target: String): Boolean

    fun findByTypeAndTarget(type: BanType, target: String): Ban?

    fun findByUuid(uuid: String): Ban?

    @Query(
        value = """
            SELECT b
            FROM Ban b
            WHERE (b.type = :accountType AND b.target = :memberId)
               OR (b.type = :phoneType  AND b.target = :phone)
            ORDER BY b.expiredAt DESC
        """
    )
    fun findByAccountOrPhone(
        @Param("accountType") accountType: BanType,
        @Param("memberId") memberId: String,
        @Param("phoneType") phoneType: BanType,
        @Param("phone") phone: String,
    ): List<Ban>

    @Query(
        value = """
            SELECT id
            FROM ban
            WHERE expired_at < :threshold
            ORDER BY id
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findAllByExpiredIds(
        @Param("threshold") threshold: Instant,
        @Param("limit") limit: Int,
    ): List<Long>

    @Modifying
    @Query(value = "DELETE FROM ban WHERE id IN (:ids)", nativeQuery = true)
    fun hardDeleteByIds(@Param("ids") ids: List<Long>): Int
}