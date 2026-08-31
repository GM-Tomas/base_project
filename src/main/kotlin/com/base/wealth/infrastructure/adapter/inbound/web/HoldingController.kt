package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateHoldingRequest
import com.base.wealth.application.dto.HoldingResponse
import com.base.wealth.application.dto.UpdateHoldingRequest
import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreateHoldingCommand
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.domain.port.inbound.PatchHoldingCommand
import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/holdings")
@Tag(name = "Holdings", description = "Endpoints para la gestión de posiciones y activos financieros")
class HoldingController(
    private val holdingUseCase: HoldingUseCase,
) {
    @GetMapping
    @Operation(summary = "Lista las posiciones del usuario, opcionalmente filtradas por clase o plataforma")
    fun getAllHoldings(
        @CurrentUser userId: UUID,
        @RequestParam(required = false) assetClass: String?,
        @RequestParam(required = false) platform: String?,
    ): ResponseEntity<List<HoldingResponse>> {
        val holdings =
            holdingUseCase.getAllHoldings(
                UserId(userId),
                assetClass?.let { AssetClass.of(it) },
                platform?.let { PlatformName.of(it) },
            )
        return ResponseEntity.ok(holdings.map { it.toResponse() })
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una posición por su ID")
    fun getHoldingById(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<HoldingResponse> =
        ResponseEntity.ok(holdingUseCase.getHoldingById(UserId(userId), HoldingId(id)).toResponse())

    @PostMapping
    @Operation(summary = "Crea una nueva posición de inversión")
    @ApiResponse(responseCode = "201")
    fun createHolding(
        @CurrentUser userId: UUID,
        @Valid @RequestBody request: CreateHoldingRequest,
    ): ResponseEntity<HoldingResponse> {
        val command =
            CreateHoldingCommand(UserId(userId), request.name, request.assetClass, request.platform, request.valueUsd)
        val created = holdingUseCase.createHolding(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(created.toResponse())
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualiza parcialmente los datos de una posición existente")
    fun updateHolding(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateHoldingRequest,
    ): ResponseEntity<HoldingResponse> {
        val command =
            PatchHoldingCommand(
                UserId(userId),
                HoldingId(id),
                request.name,
                request.assetClass,
                request.platform,
                request.valueUsd,
            )
        val updated = holdingUseCase.updateHolding(command)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una posición financiera")
    @ApiResponse(responseCode = "204")
    fun deleteHolding(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        holdingUseCase.deleteHolding(UserId(userId), HoldingId(id))
        return ResponseEntity.noContent().build()
    }

    private fun Holding.toResponse() =
        HoldingResponse(
            id = id.value.toString(),
            name = name,
            assetClass = assetClass.value,
            platform = platform.value,
            valueUsd = value.amount.toDouble(),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
