package com.blueoauld.server.point.application.port

interface AttendanceStore {

    fun claim(memberId: Long, deviceId: String): Boolean

    fun isAttend(memberId: Long, deviceId: String): Boolean
}