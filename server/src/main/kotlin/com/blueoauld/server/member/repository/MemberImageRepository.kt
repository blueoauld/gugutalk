package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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
}