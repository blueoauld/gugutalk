package com.blueoauld.server.support

import com.blueoauld.server.common.authentication.AuthenticationAttributes
import org.springframework.test.web.servlet.request.RequestPostProcessor

/**
 * 컨트롤러 슬라이스(@WebMvcTest) 테스트 지원.
 *
 * 하드코딩 인증(AuthenticationFilter)은 슬라이스에서 동작하지 않으므로,
 * 실제 [com.blueoauld.server.common.authentication.infrastructure.AuthenticationPrincipalArgumentResolver]
 * 가 읽는 요청 속성(MEMBER_ID)을 직접 세팅해 `@Login` 을 해석하게 한다.
 * JWT/X-Device-Id 없이 로그인 상태를 흉내 낸다.
 */
object WebMvcTestSupport {

    fun withLogin(memberId: Long = 1L) = RequestPostProcessor { request ->
        request.setAttribute(AuthenticationAttributes.MEMBER_ID, memberId)
        request
    }
}
