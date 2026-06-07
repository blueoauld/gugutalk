package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository : JpaRepository<Review, Long>, ReviewCustomRepository {

    @Modifying
    @Query(
        value = "DELETE FROM review WHERE from_id IN (:ids) OR to_id IN (:ids)",
        nativeQuery = true,
    )
    fun deleteByMemberIds(@Param("ids") ids: List<Long>): Int
}