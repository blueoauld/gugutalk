package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Block
import org.springframework.data.jpa.repository.JpaRepository

interface BlockRepository : JpaRepository<Block, Long>