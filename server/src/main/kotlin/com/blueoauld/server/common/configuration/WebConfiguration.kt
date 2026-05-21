package com.blueoauld.server.common.configuration

import com.blueoauld.server.common.authentication.infrastructure.AuthenticationPrincipalArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(

    private val authenticationPrincipalArgumentResolver: AuthenticationPrincipalArgumentResolver,
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticationPrincipalArgumentResolver)
    }

    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.useRequestHeader("Api-Version")
            .addSupportedVersions("1", "2")
            .setVersionRequired(false)
            .setDefaultVersion("1")
    }
}