package com.blueoauld.server.activity.repository

import com.blueoauld.server.activity.entity.Unlike
import org.springframework.data.jpa.repository.JpaRepository

interface UnlikeRepository : JpaRepository<Unlike, Long>