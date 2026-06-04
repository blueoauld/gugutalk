package com.blueoauld.server.ban.repository

import com.blueoauld.server.ban.entity.Ban
import com.blueoauld.server.ban.entity.type.BanType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BanRepository : JpaRepository<Ban, Long> {

    fun existsByTypeAndTarget(type: BanType, target: String): Boolean

    fun findByUuid(uuid: String): Ban?

    @Query(
        value = """
            SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
            FROM Ban b
            WHERE (b.type = :accountType AND b.target = :memberId)
               OR (b.type = :phoneType  AND b.target = :phone)
        """
    )
    fun existsAccountOrPhone(
        @Param("accountType") accountType: BanType,
        @Param("memberId") memberId: String,
        @Param("phoneType") phoneType: BanType,
        @Param("phone") phone: String,
    ): Boolean
}