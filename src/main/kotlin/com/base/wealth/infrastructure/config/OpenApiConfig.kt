package com.base.wealth.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("BASE Wealth Management API")
                    .version("1.0.0")
                    .description(
                        "REST API construida con Kotlin y Spring Boot 3 para gestión de activos " +
                            "patrimoniales y proyecciones financieras.",
                    ),
            )
}
