package com.blueoauld.server.point.repository

import com.blueoauld.server.point.entity.Point
import org.springframework.data.jpa.repository.JpaRepository

interface PointRepository : JpaRepository<Point, Long> {

    fun findByMemberId(memberId: Long): Point?
}