package com.base.wealth.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("BASE Wealth Management & Tasks API")
                    .version("1.0.0")
                    .description(
                        "REST API construida con Kotlin y Spring Boot 3 para gestión de activos patrimoniales, proyecciones financieras y tareas.",
                    ).contact(
                        Contact()
                            .name("BASE Engineering Team")
                            .email("dev@base-wealth.internal"),
                    ).license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0"),
                    ),
            )
}
