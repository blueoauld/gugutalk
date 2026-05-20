package com.blueoauld.server.authentication.repository

import com.blueoauld.server.authentication.entity.VerificationCode
import org.springframework.data.jpa.repository.JpaRepository

interface VerificationCodeRepository : JpaRepository<VerificationCode, Long>