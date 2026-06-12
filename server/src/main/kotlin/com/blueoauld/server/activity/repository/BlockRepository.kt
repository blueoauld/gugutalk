package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Block
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BlockRepository : JpaRepository<Block, Long>, BlockCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int

    @Query(
        value = """
        SELECT count(b) > 0 
        FROM Block b
        WHERE (b.fromId = :member1Id AND b.toId = :member2Id) 
           OR (b.fromId = :member2Id AND b.toId = :member1Id)
    """
    )
    fun existsBlockBetween(
        @Param("member1Id") member1Id: Long,
        @Param("member2Id") member2Id: Long,
    ): Boolean

    @Modifying
    @Query("DELETE FROM Block b WHERE b.fromId IN :ids OR b.toId IN :ids")
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}