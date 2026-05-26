package com.blueoauld.server.report.application.event

data class ReportCreateEvent(

    val reportId: Long,
    val keys: List<String>,
)
