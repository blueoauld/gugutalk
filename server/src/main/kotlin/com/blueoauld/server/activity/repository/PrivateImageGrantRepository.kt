package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.PrivateImageGrant
import org.springframework.data.jpa.repository.JpaRepository

interface PrivateImageGrantRepository : JpaRepository<PrivateImageGrant, Long>, PrivateImageGrantCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int
}