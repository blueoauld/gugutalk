package com.blueoauld.server.common.authentication.filter

import com.blueoauld.server.authentication.application.AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY
import com.blueoauld.server.common.authentication.application.TokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthenticationFilter(

    private val tokenProvider: TokenProvider,
    private val stringRedisTemplate: StringRedisTemplate,
) : OncePerRequestFilter() {

    private val antPathMatcher = AntPathMatcher()
    private val whitelist = listOf(
        HttpMethod.POST to "/api/authentications/verification-code",
        HttpMethod.POST to "/api/authentications/signup",
        HttpMethod.POST to "/api/authentications/login",
    )

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (matches(whitelist, request)) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken = resolveAccessToken(request)
        val memberId = accessToken?.let { tokenProvider.parseAndValidate(it) }

        if (accessToken == null || memberId == null) {
            exception(response)
            return
        }

        val accessTokenBlacklistKey = AUTHENTICATION_ACCESS_TOKEN_BLACKLIST_KEY + accessToken
        if (stringRedisTemplate.hasKey(accessTokenBlacklistKey)) {
            exception(response)
            return
        }

        request.setAttribute("memberId", memberId)
        filterChain.doFilter(request, response)
    }

    private fun matches(patterns: List<Pair<HttpMethod, String>>, request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        val method = HttpMethod.valueOf(request.method)

        return patterns.any { (m, pattern) ->
            m == method && antPathMatcher.match(pattern, uri)
        }
    }

    private fun resolveAccessToken(servletRequest: HttpServletRequest): String? {
        val authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (authorizationHeader.startsWith("Bearer ")) authorizationHeader.substring(7) else null
    }

    private fun exception(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write("""{"code": "UNAUTHORIZED_01", "message": "인증이 필요합니다."}""")
    }
}