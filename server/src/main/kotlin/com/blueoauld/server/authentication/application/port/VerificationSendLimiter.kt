package com.blueoauld.server.authentication.application.port

interface VerificationSendLimiter {

    fun isExceeded(deviceId: String): Boolean

    fun record(deviceId: String)
}