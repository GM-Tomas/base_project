package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.domain.model.PlatformMeta
import com.base.wealth.domain.port.inbound.PlatformUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/platforms")
@Tag(name = "Platforms", description = "Endpoints de plataformas y entidades financieras")
class PlatformController(
    private val platformUseCase: PlatformUseCase
) {

    @GetMapping
    @Operation(summary = "Lista todas las plataformas registradas")
    fun getAllPlatforms(): ResponseEntity<List<PlatformMeta>> {
        return ResponseEntity.ok(platformUseCase.getAllPlatforms())
    }
}
