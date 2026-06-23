package com.blueoauld.server.support

import com.eatthepath.pushy.apns.ApnsClient
import com.google.crypto.tink.apps.rewardedads.RewardedAdsVerifier
import io.mockk.mockk
import net.dv8tion.jda.api.JDA
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * 통합 테스트 공용 베이스.
 *
 * Postgres + Redis 컨테이너를 컴패니언에서 한 번만 띄워(suite 전역) 재사용하고
 * `@DynamicPropertySource` 로 datasource/redis 를 주입한다. Flyway 가 컨테이너에
 * 마이그레이션을 적용하므로 모든 통합 테스트는 실제 스키마 검증도 겸한다.
 *
 * 외부 연동(JDA/APNs/AdMob)은 시작 시 네트워크에 연결하려 하므로 목 빈으로 대체한다.
 */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@Testcontainers
@Import(IntegrationTestSupport.ExternalIntegrationMocks::class)
abstract class IntegrationTestSupport {

    @TestConfiguration
    class ExternalIntegrationMocks {

        @Bean
        fun jda(): JDA = mockk(relaxed = true)

        @Bean
        fun apnsClient(): ApnsClient = mockk(relaxed = true)

        @Bean
        fun rewardedAdsVerifier(): RewardedAdsVerifier = mockk(relaxed = true)
    }

    companion object {

        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply { start() }

        @JvmStatic
        private val redis = GenericContainer("redis:7-alpine").withExposedPorts(6379).apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)

            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
            registry.add("spring.data.redis.password") { "" }
        }
    }
}
