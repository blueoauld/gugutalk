package com.blueoauld.server.authentication.application.port

interface RefreshTokenStore {

    fun save(memberId: Long, token: String)
}