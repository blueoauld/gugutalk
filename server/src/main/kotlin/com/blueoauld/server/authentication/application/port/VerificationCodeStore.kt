package com.blueoauld.server.authentication.application.port

interface VerificationCodeStore {

    fun save(phone: String, code: String)

    fun get(phone: String): String?

    fun delete(phone: String)
}