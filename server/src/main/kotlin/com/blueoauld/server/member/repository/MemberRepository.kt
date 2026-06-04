package com.blueoauld.server.member.repository

import com.blueoauld.server.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long>, MemberCustomRepository {

    fun existsByPhone(phone: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByPhone(phone: String): Member?

    fun findByNickname(nickname: String): Member?
}