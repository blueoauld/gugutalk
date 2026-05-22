package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long>