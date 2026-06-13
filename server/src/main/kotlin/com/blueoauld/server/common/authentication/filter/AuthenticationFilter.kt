package com.blueoauld.server.common.authentication.filter

import com.blueoauld.server.authentication.application.port.AccessTokenBlacklistStore
import com.blueoauld.server.ban.entity.type.BanType.*
import com.blueoauld.server.ban.repository.BanRepository
import com.blueoauld.server.common.authentication.AuthenticationAttributes
import com.blueoauld.server.common.authentication.application.TokenProvider
import com.blueoauld.server.member.repository.MemberRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.ObjectMapper
import java.time.Instant

@Component
class AuthenticationFilter(

    private val memberRepository: MemberRepository,
    private val banRepository: BanRepository,
    private val tokenProvider: TokenProvider,
    private val accessTokenBlacklistStore: AccessTokenBlacklistStore,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    private val antPathMatcher = AntPathMatcher()
    private val whitelist = listOf(
        HttpMethod.POST to "/api/authentication/verify",
        HttpMethod.POST to "/api/authentication/signup",
        HttpMethod.POST to "/api/authentication/login",
        HttpMethod.POST to "/api/authentication/token/rotate",
    )
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
        val deviceId = resolveDeviceId(request)
        if (deviceId == null) {
            exception(response, "존재하지 않는 디바이스 ID입니다.")
            return
        }
        banRepository.findByTypeAndTarget(DEVICE, deviceId)?.let {
            return exception(response, it.uuid, it.reason, it.expiredAt)
        }

        if (matches(whitelist, request)) {
            filterChain.doFilter(request, response)
            return
        }

        val accessToken = resolveAccessToken(request)
        val memberId = accessToken?.let { tokenProvider.parseAndValidate(it) }

        if (accessToken == null || memberId == null) {
            exception(response, "유효하지 않은 토큰입니다.")
            return
        }
        if (accessTokenBlacklistStore.contain(accessToken)) {
            exception(response, "이미 로그아웃된 토큰입니다.")
            return
        }

        val member = memberRepository.findByIdOrNull(memberId) ?: return exception(response, "존재하지 않는 회원입니다.")

        banRepository.findByAccountOrPhone(ACCOUNT, memberId.toString(), PHONE, member.phone)
            .firstOrNull()?.let {
                return exception(response, it.uuid, it.reason, it.expiredAt)
            }

        MDC.put(AuthenticationAttributes.MEMBER_ID, memberId.toString())
        MDC.put(AuthenticationAttributes.NICKNAME, member.nickname)

        request.setAttribute(AuthenticationAttributes.MEMBER_ID, memberId)
        filterChain.doFilter(request, response)
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

    private fun resolveAccessToken(servletRequest: HttpServletRequest): String? {
        val authorizationHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (authorizationHeader.startsWith("Bearer ")) authorizationHeader.substring(7) else null
    }

    private fun resolveDeviceId(servletRequest: HttpServletRequest): String? {
        return servletRequest.getHeader("X-Device-Id")
    }

    private fun exception(response: HttpServletResponse, uuid: String, reason: String, expiredAt: Instant) {
        writeBody(
            response, mapOf(
                "code" to "UNAUTHORIZED_03",
                "uuid" to uuid,
                "reason" to reason,
                "expiredAt" to expiredAt.toString(),
            )
        )
    }

    private fun exception(response: HttpServletResponse, message: String) {
        writeBody(response, mapOf("code" to "UNAUTHORIZED_01", "message" to message))
    }

    private fun writeBody(response: HttpServletResponse, body: Map<String, Any?>) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        objectMapper.writeValue(response.writer, body)
    }
}