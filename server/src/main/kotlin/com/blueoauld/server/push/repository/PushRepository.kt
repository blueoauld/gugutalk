package com.blueoauld.server.push.repository

import com.blueoauld.server.push.entity.Push
import org.springframework.data.jpa.repository.JpaRepository

interface PushRepository : JpaRepository<Push, Long> {

    fun findByToken(token: String): Push?

    fun deleteByToken(token: String)
}