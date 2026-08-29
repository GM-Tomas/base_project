package com.base.wealth.application.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateHoldingRequest(
    @field:NotBlank(message = "El nombre del activo no puede estar vacío")
    val name: String,
    @field:NotBlank(message = "La clase de activo es obligatoria")
    val assetClass: String,
    @field:NotBlank(message = "La plataforma es obligatoria")
    val platform: String,
    @field:NotNull(message = "El valor es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "El valor no puede ser negativo")
    val valueUsd: Double,
)

data class UpdateHoldingRequest(
    val name: String? = null,
    val assetClass: String? = null,
    val platform: String? = null,
    val valueUsd: Double? = null,
)

// Response shape only — never the domain Holding directly (it carries userId, and its Money/id
// value classes aren't meant to dictate the wire format). Field names/shape match
// specs/001-backend-para-frontend/contracts/openapi.yaml's Holding schema exactly (id is a UUID
// string, not a Long — the one contract change forced by real per-user persistence, Fase 2).
data class HoldingResponse(
    val id: String,
    val name: String,
    val assetClass: String,
    val platform: String,
    val valueUsd: Double,
    val createdAt: Instant,
    val updatedAt: Instant,
)
