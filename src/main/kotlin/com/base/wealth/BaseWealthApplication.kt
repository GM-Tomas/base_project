package com.base.wealth

import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

// Datasource auto-configuration is excluded by default (see application.yml's base
// `spring.autoconfigure.exclude`) and re-enabled per profile: "dev" picks it up from
// compose.yaml via spring-boot-docker-compose, "test" from Testcontainers (see
// support/PostgresTestBase), and "prod" wires its own DataSource bean manually
// (infrastructure.config.DataSourceConfig) regardless of the exclusion. Profile-scoped
// YAML can flip this per-profile; a static `exclude = [...]` on this annotation can't.
@SpringBootApplication
@ConfigurationPropertiesScan
class BaseWealthApplication {
    // A real java.time.Clock, not a hand-rolled port: services take Clock directly and tests
    // swap it for Clock.fixed(...) — no adapter needed for something the JDK already provides.
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}

fun main(args: Array<String>) {
    // springdoc has no idea @CurrentUser resolves from the verified JWT (CurrentUserArgumentResolver)
    // — left alone, it documents it as a public required "userId" query parameter on every endpoint,
    // exactly what CA-01.5 says the API must never accept. Must run before the context refreshes:
    // springdoc's own beans read this registry once, at their own construction.
    SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser::class.java)
    runApplication<BaseWealthApplication>(*args)
}
