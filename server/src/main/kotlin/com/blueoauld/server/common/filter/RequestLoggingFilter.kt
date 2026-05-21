package com.blueoauld.server.common.filter

import com.blueoauld.server.common.util.IpExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsed = System.currentTimeMillis() - startTime
            val ip = IpExtractor.extract(request)
            val status = response.status

            val message = "METHOD=${request.method} URI=${request.requestURI} IP=$ip STATUS=$status ${elapsed}ms"

            when {
                status >= 500 -> log.error { message }
                status >= 400 -> log.warn { message }
                else -> log.info { message }
            }
        }
    }
}