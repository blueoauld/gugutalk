package com.blueoauld.server.authentication.application.port

fun interface AccessTokenBlacklistStore {

    fun save(memberId: Long, token: String)
}