package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Block
import org.springframework.data.jpa.repository.JpaRepository

interface BlockRepository : JpaRepository<Block, Long>, BlockCustomRepository {

    fun existsByFromIdAndToId(fromId: Long, toId: Long): Boolean

    fun deleteByFromIdAndToId(fromId: Long, toId: Long): Int
}