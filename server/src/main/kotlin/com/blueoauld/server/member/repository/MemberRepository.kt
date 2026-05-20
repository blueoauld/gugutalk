package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByPhone(phone: String): Boolean
}