package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.PrivateImageGrant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PrivateImageGrantRepository : JpaRepository<PrivateImageGrant, Long>, PrivateImageGrantCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int

    @Modifying
    @Query("DELETE FROM PrivateImageGrant pig WHERE pig.fromId IN :ids OR pig.toId IN :ids")
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}