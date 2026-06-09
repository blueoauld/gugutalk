package com.blueoauld.server.point.application.port

interface AttendanceStore {

    fun isAttend(memberId: Long): Boolean

    fun mark(memberId: Long)
}