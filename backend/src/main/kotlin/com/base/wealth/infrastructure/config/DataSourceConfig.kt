package com.base.wealth.infrastructure.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

// Only active in prod (see BaseWealthApplication, which turns off Spring's own
// DataSource auto-config so local/dev can run with no database at all). This is Spring
// Boot's own documented recipe for wiring a DataSource by hand: bind spring.datasource.*
// onto DataSourceProperties, then bind spring.datasource.hikari.* onto the pool itself.
@Configuration
@Profile("prod")
@EnableConfigurationProperties(DataSourceProperties::class)
class DataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    fun dataSource(properties: DataSourceProperties): DataSource =
        properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

    @Bean
    fun jdbcTemplate(dataSource: DataSource): JdbcTemplate = JdbcTemplate(dataSource)
}
