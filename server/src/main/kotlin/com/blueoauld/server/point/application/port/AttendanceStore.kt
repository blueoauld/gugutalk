package com.blueoauld.server.point.application.port

interface AttendanceStore {

    fun isAttend(deviceId: String): Boolean

    fun mark(memberId: Long, deviceId: String)
}