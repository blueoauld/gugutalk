package com.blueoauld.server.common.util

import jakarta.servlet.http.HttpServletRequest

object IpExtractor {

    private val IP_HEADERS = listOf(
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
    )

    fun extract(request: HttpServletRequest): String {
        for (header in IP_HEADERS) {
            val ip = request.getHeader(header)

            if (!ip.isNullOrBlank() && !ip.equals("unknown", ignoreCase = true)) {
                return ip.split(",").first().trim()
            }
        }
        return request.remoteAddr
    }
}