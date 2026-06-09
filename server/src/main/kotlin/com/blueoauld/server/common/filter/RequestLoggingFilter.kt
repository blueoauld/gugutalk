package com.blueoauld.server.common.filter

import com.blueoauld.server.common.util.IpExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    private val antPathMatcher = AntPathMatcher()
    private val exclude = listOf(
        HttpMethod.GET to "/ws/**",
        HttpMethod.GET to "/actuator/**",
        HttpMethod.DELETE to "/api/push",
    )
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

            log.info {
                "METHOD = ${request.method}, URI = ${request.requestURI}, IP = $ip, STATUS = ${response.status}, MS = $elapsed"
            }
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