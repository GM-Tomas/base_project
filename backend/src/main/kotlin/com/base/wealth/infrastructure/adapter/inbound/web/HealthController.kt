package com.base.wealth.infrastructure.adapter.inbound.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthStatusResponse(
    val status: String,
    val service: String,
    val timestamp: Instant,
    val environment: String,
    val version: String
)

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Monitoreo del estado y disponibilidad de la API")
class HealthController {

    @GetMapping
    @Operation(summary = "Verifica el estado operativo del servicio backend")
    fun checkHealth(): ResponseEntity<HealthStatusResponse> {
        val response = HealthStatusResponse(
            status = "UP",
            service = "base-wealth-backend",
            timestamp = Instant.now(),
            environment = "production-ready",
            version = "1.0.0"
        )
        return ResponseEntity.ok(response)
    }
}
