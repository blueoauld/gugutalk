package com.blueoauld.server.point.repository

import com.blueoauld.server.point.entity.PointHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryRepository : JpaRepository<PointHistory, Long>, PointHistoryCustomRepository