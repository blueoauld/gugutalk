package com.blueoauld.server.authentication.application.port

interface AccessTokenBlacklistStore {

    fun save(memberId: Long, token: String)

    fun contain(token: String): Boolean
}