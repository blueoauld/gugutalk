package com.blueoauld.server.common.filter

import com.blueoauld.server.common.authentication.AuthenticationAttributes
import com.blueoauld.server.common.util.IpExtractor
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.logstash.logback.argument.StructuredArguments.value
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

private const val REQUEST_ID = "requestId"
private const val METHOD = "method"
private const val URI = "uri"
private const val IP = "ip"

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
        MDC.put(METHOD, request.method)
        MDC.put(URI, request.requestURI)
        MDC.put(IP, IpExtractor.extract(request))

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsed = System.currentTimeMillis() - startTime

            log.atInfo {
                message = "STATUS={}, MS={}"
                arguments = arrayOf(value("status", response.status), value("ms", elapsed))
            }

            MDC.remove(REQUEST_ID)
            MDC.remove(METHOD)
            MDC.remove(URI)
            MDC.remove(IP)
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