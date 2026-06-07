package com.blueoauld.server.point.repository

import com.blueoauld.server.point.entity.PointHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PointHistoryRepository : JpaRepository<PointHistory, Long>, PointHistoryCustomRepository {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PointHistory ph WHERE ph.pointId = :pointId")
    fun deleteAllByPointId(@Param("pointId") pointId: Long): Int
}