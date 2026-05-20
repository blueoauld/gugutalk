package com.blueoauld.server.common.util

import java.security.SecureRandom

object RandomNumberGenerator {

    private val secureRandom = SecureRandom()

    fun generateSixDigitCode(): String {
        return String.format("%06d", secureRandom.nextInt(1000000))
    }
}