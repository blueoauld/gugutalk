package com.blueoauld.server.ban.repository

import com.blueoauld.server.ban.entity.Ban
import com.blueoauld.server.ban.entity.type.BanType
import org.springframework.data.jpa.repository.JpaRepository

interface BanRepository : JpaRepository<Ban, Long> {

    fun existsByTypeAndTarget(type: BanType, target: String): Boolean

    fun findByUuid(uuid: String): Ban?
}