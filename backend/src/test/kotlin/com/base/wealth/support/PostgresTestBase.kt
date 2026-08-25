package com.base.wealth.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer

/**
 * Base class for integration tests that need a real Postgres. Extend it instead of writing
 * @SpringBootTest directly: it activates the "test" profile (see application.yml, which clears
 * the DataSource auto-config exclusion for it) and wires an ephemeral, real Postgres container
 * as the DataSource via Spring Boot's @ServiceConnection support — no manual JDBC URL/driver
 * wiring, and no effect on plain @SpringBootTest classes that don't extend this.
 *
 * Requires a Docker daemon reachable by Testcontainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(PostgresTestBase.PostgresContainerConfig::class)
abstract class PostgresTestBase {
    @TestConfiguration(proxyBeanMethods = false)
    class PostgresContainerConfig {
        @Bean
        @ServiceConnection
        fun postgresContainer(): PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                // auth.users/public.tasks are real in Supabase (created by supabase/schema.sql,
                // outside Flyway) but don't exist on a bare image — V2's FKs and V4's index need
                // a stand-in. See testcontainers-init.sql.
                .withInitScript("testcontainers-init.sql")
    }
}
