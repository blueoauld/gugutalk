package com.blueoauld.server.push.repository

import com.blueoauld.server.push.entity.Push
import org.springframework.data.jpa.repository.JpaRepository

interface PushRepository : JpaRepository<Push, Long> {

    fun findByToken(token: String): Push?

    fun findAllByMemberId(memberId: Long): List<Push>

    fun deleteByToken(token: String)
}