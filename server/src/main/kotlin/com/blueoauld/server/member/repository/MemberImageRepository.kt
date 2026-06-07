package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberImageRepository : JpaRepository<MemberImage, Long> {

    @Query(
        value = """
            SELECT mi
            FROM MemberImage mi
            WHERE mi.memberId = :memberId
            ORDER BY mi.type, mi.sortOrder
        """
    )
    fun findAllByMemberId(memberId: Long): List<MemberImage>

    @Query(
        value = """
            SELECT mi
            FROM MemberImage mi
            WHERE mi.memberId = :memberId AND mi.type = :type
            ORDER BY mi.sortOrder
        """
    )
    fun findAllByMemberIdAndType(
        memberId: Long,
        type: MemberImageType,
    ): List<MemberImage>

    @Query("SELECT mi.key FROM MemberImage mi WHERE mi.memberId IN :ids")
    fun findKeysByMemberIds(@Param("ids") ids: List<Long>): List<String>

    @Modifying
    @Query("DELETE FROM MemberImage mi WHERE mi.memberId IN :ids")
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}