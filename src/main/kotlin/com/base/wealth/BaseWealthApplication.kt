package com.base.wealth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

// Datasource auto-configuration is excluded by default (see application.yml's base
// `spring.autoconfigure.exclude`) and re-enabled per profile: "dev" picks it up from
// compose.yaml via spring-boot-docker-compose, "test" from Testcontainers (see
// support/PostgresTestBase), and "prod" wires its own DataSource bean manually
// (infrastructure.config.DataSourceConfig) regardless of the exclusion. Profile-scoped
// YAML can flip this per-profile; a static `exclude = [...]` on this annotation can't.
@SpringBootApplication
@ConfigurationPropertiesScan
class BaseWealthApplication

fun main(args: Array<String>) {
    runApplication<BaseWealthApplication>(*args)
}
