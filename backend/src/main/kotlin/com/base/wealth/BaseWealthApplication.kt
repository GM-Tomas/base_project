package com.base.wealth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.runApplication

// DataSource/JdbcTemplate are wired manually (see infrastructure.config.DataSourceConfig) and only
// under the "prod" profile — local/dev runs against FileKvStore with no database configured at all,
// so the default auto-config (which demands a datasource url) must stay off.
@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        JdbcTemplateAutoConfiguration::class
    ]
)
class BaseWealthApplication

fun main(args: Array<String>) {
    runApplication<BaseWealthApplication>(*args)
}
