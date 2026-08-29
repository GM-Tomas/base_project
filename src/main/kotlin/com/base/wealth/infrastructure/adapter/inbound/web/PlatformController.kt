package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreatePlatformRequest
import com.base.wealth.application.dto.PatchPlatformRequest
import com.base.wealth.application.dto.PlatformResponse
import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreatePlatformCommand
import com.base.wealth.domain.port.inbound.PatchPlatformCommand
import com.base.wealth.domain.port.inbound.PlatformUseCase
import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/platforms")
@Tag(name = "Platforms", description = "Endpoints de plataformas y entidades financieras")
class PlatformController(
    private val platformUseCase: PlatformUseCase,
) {
    @GetMapping
    @Operation(summary = "Lista todas las plataformas del usuario, incluidas las que no tienen posiciones")
    fun getAllPlatforms(
        @CurrentUser userId: UUID,
    ): ResponseEntity<List<PlatformResponse>> =
        ResponseEntity.ok(
            platformUseCase.getAllPlatforms(UserId(userId)).map {
                it.toResponse()
            },
        )

    @PostMapping
    @Operation(summary = "Crea una nueva plataforma")
    fun createPlatform(
        @CurrentUser userId: UUID,
        @Valid @RequestBody request: CreatePlatformRequest,
    ): ResponseEntity<PlatformResponse> {
        val created = platformUseCase.createPlatform(CreatePlatformCommand(UserId(userId), request.name, request.type))
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }

    @PatchMapping("/{name}")
    @Operation(summary = "Renombra o recategoriza una plataforma existente")
    fun patchPlatform(
        @CurrentUser userId: UUID,
        @PathVariable name: String,
        @RequestBody request: PatchPlatformRequest,
    ): ResponseEntity<PlatformResponse> {
        val updated =
            platformUseCase.patchPlatform(
                PatchPlatformCommand(UserId(userId), name, request.name, request.type),
            )
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Elimina una plataforma sin posiciones asociadas")
    fun deletePlatform(
        @CurrentUser userId: UUID,
        @PathVariable name: String,
    ): ResponseEntity<Void> {
        platformUseCase.deletePlatform(UserId(userId), name)
        return ResponseEntity.noContent().build()
    }

    private fun Platform.toResponse() = PlatformResponse(name = name.value, type = type.value, createdAt = createdAt)
}
