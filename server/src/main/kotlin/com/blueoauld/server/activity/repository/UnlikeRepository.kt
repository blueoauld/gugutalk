package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Unlike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UnlikeRepository : JpaRepository<Unlike, Long>, UnlikeCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int

    @Modifying
    @Query("DELETE FROM Unlike u WHERE u.fromId IN :ids OR u.toId IN :ids")
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}