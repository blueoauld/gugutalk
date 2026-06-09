package com.blueoauld.server.authentication.application.port

fun interface MessageSender {

    fun send(to: String, code: String)
}