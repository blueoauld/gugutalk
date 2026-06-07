package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Like
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface LikeRepository : JpaRepository<Like, Long>, LikeCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int

    @Modifying
    @Query("DELETE FROM Like l WHERE l.fromId IN :ids OR l.toId IN :ids")
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}