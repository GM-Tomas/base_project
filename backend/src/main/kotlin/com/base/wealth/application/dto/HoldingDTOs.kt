package com.base.wealth.application.dto

import com.base.wealth.domain.model.AssetClass
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateHoldingRequest(
    @field:NotBlank(message = "El nombre del activo no puede estar vacío")
    val name: String,

    @field:NotBlank(message = "La clase de activo es obligatoria")
    val cls: AssetClass,

    @field:NotBlank(message = "La plataforma es obligatoria")
    val platform: String,

    @field:NotNull(message = "El valor es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "El valor no puede ser negativo")
    val value: Double
)

data class UpdateHoldingRequest(
    val name: String? = null,
    val cls: AssetClass? = null,
    val platform: String? = null,
    val value: Double? = null
)
