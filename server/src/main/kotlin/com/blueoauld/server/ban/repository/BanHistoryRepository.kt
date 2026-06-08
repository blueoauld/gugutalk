package com.blueoauld.server.ban.repository

import com.blueoauld.server.ban.entity.BanHistory
import org.springframework.data.jpa.repository.JpaRepository

interface BanHistoryRepository : JpaRepository<BanHistory, Long>