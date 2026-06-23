package com.blueoauld.server.support

import com.blueoauld.server.common.authentication.filter.AuthenticationFilter
import com.blueoauld.server.common.authentication.infrastructure.AuthenticationPrincipalArgumentResolver
import com.blueoauld.server.common.configuration.WebConfiguration
import com.blueoauld.server.common.exception.GlobalExceptionHandler
import com.blueoauld.server.common.filter.RequestLoggingFilter
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * 컨트롤러 슬라이스 테스트용 합성 애너테이션.
 *
 * @WebMvcTest 가 끌어오는 Filter 빈(하드코딩 인증의 [AuthenticationFilter] / [RequestLoggingFilter])은
 * 슬라이스에 없는 의존성을 요구해 컨텍스트 부팅을 막으므로 제외하고, API 버전 라우팅([WebConfiguration])·
 * @Login 리졸버·전역 예외 처리를 함께 로드한다.
 *
 * 대상 컨트롤러는 [controllers] 로 지정한다(= @WebMvcTest 의 controllers 로 위임).
 * 서비스는 각 테스트에서 @Import 한 @TestConfiguration 의 MockK 빈으로 대체한다.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@WebMvcTest(
    excludeFilters = [
        Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [AuthenticationFilter::class, RequestLoggingFilter::class],
        ),
    ],
)
@Import(
    WebConfiguration::class,
    AuthenticationPrincipalArgumentResolver::class,
    GlobalExceptionHandler::class,
)
annotation class ControllerSliceTest(

    @get:AliasFor(annotation = WebMvcTest::class, attribute = "controllers")
    vararg val controllers: KClass<*> = [],
)
