package com.blueoauld.server.point.repository

import com.blueoauld.server.point.entity.RewardTransaction
import org.springframework.data.jpa.repository.JpaRepository

interface RewardTransactionRepository : JpaRepository<RewardTransaction, String>