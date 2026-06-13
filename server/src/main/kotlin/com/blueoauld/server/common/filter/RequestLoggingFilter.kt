package com.blueoauld.server.common.filter

import com.blueoauld.server.common.authentication.AuthenticationAttributes
import com.blueoauld.server.common.util.IpExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private const val REQUEST_ID = "requestId"

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}
    private val antPathMatcher = AntPathMatcher()
    private val exclude = listOf(
        HttpMethod.GET to "/ws/**",
        HttpMethod.GET to "/actuator/**",
        HttpMethod.GET to "/admob-ssv",
        HttpMethod.DELETE to "/api/push",
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()
        MDC.put(REQUEST_ID, UUID.randomUUID().toString().take(8))

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsed = System.currentTimeMillis() - startTime
            val ip = IpExtractor.extract(request)

            log.info {
                "METHOD = ${request.method}, URI = ${request.requestURI}, IP = $ip, STATUS = ${response.status}, MS = $elapsed"
            }

            MDC.remove(REQUEST_ID)
            MDC.remove(AuthenticationAttributes.MEMBER_ID)
            MDC.remove(AuthenticationAttributes.NICKNAME)
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return matches(exclude, request)
    }

    private fun matches(patterns: List<Pair<HttpMethod, String>>, request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        val method = HttpMethod.valueOf(request.method)

        return patterns.any { (m, pattern) ->
            m == method && antPathMatcher.match(pattern, uri)
        }
    }
}