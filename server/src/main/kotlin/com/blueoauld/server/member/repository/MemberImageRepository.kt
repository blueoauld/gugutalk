package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.MemberImage
import com.blueoauld.server.member.entity.type.MemberImageType
import org.springframework.data.jpa.repository.JpaRepository

interface MemberImageRepository : JpaRepository<MemberImage, Long> {

    fun findAllByMemberIdAndType(memberId: Long, type: MemberImageType): List<MemberImage>
}