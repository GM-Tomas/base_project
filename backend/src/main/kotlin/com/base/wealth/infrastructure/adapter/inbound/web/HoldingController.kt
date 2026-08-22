package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateHoldingRequest
import com.base.wealth.application.dto.UpdateHoldingRequest
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.port.inbound.HoldingUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/holdings")
@Tag(name = "Holdings", description = "Endpoints para la gestión de posiciones y activos financieros")
class HoldingController(
    private val holdingUseCase: HoldingUseCase
) {

    @GetMapping
    @Operation(summary = "Lista todas las posiciones financieras")
    fun getAllHoldings(): ResponseEntity<List<Holding>> {
        return ResponseEntity.ok(holdingUseCase.getAllHoldings())
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de una posición por su ID")
    fun getHoldingById(@PathVariable id: Long): ResponseEntity<Holding> {
        return ResponseEntity.ok(holdingUseCase.getHoldingById(id))
    }

    @PostMapping
    @Operation(summary = "Crea una nueva posición de inversión")
    fun createHolding(@Valid @RequestBody request: CreateHoldingRequest): ResponseEntity<Holding> {
        val created = holdingUseCase.createHolding(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de una posición existente")
    fun updateHolding(
        @PathVariable id: Long,
        @RequestBody request: UpdateHoldingRequest
    ): ResponseEntity<Holding> {
        val updated = holdingUseCase.updateHolding(id, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una posición financiera")
    fun deleteHolding(@PathVariable id: Long): ResponseEntity<Void> {
        holdingUseCase.deleteHolding(id)
        return ResponseEntity.noContent().build()
    }
}
