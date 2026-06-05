package com.blueoauld.server.common.configuration

import com.blueoauld.server.common.properties.ApnsProperties
import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class ApnsConfiguration(

    private val apnsProperties: ApnsProperties,
    private val resourceLoader: ResourceLoader,
) {

    @Bean
    fun apnsClient(): ApnsClient {
        val signingKey = resourceLoader.getResource(apnsProperties.keyPath).inputStream.use { input ->
            ApnsSigningKey.loadFromInputStream(input, apnsProperties.teamId, apnsProperties.keyId)
        }

        val host = if (apnsProperties.production) {
            ApnsClientBuilder.PRODUCTION_APNS_HOST
        } else {
            ApnsClientBuilder.DEVELOPMENT_APNS_HOST
        }

        return ApnsClientBuilder()
            .setApnsServer(host)
            .setSigningKey(signingKey)
            .build()
    }
}