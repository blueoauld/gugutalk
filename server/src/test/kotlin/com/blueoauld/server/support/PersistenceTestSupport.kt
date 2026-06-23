package com.blueoauld.server.support

import com.blueoauld.server.common.configuration.JdslConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * 영속성 슬라이스 공용 베이스.
 *
 * `@DataJpaTest` 로 JPA 계층만 띄우고, 실제 Postgres 컨테이너(H2 아님)에 Flyway 마이그레이션을
 * 적용한 스키마 위에서 리포지토리/JDSL 쿼리를 검증한다. JDSL 렌더 컨텍스트가 필요하므로
 * [JdslConfiguration] 을 임포트한다. 커스텀 임플(JDSL)은 각 테스트에서 `@Import` 한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JdslConfiguration::class)
abstract class PersistenceTestSupport {

    companion object {

        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:16-alpine").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
