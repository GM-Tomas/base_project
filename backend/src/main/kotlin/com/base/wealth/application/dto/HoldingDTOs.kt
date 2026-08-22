package com.base.wealth.application.dto

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Currency
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateHoldingRequest(
    @field:NotBlank(message = "El nombre del activo no puede estar vacío")
    val name: String,

    @field:NotNull(message = "La clase de activo es obligatoria")
    val cls: AssetClass,

    @field:NotBlank(message = "La plataforma es obligatoria")
    val platform: String,

    val qty: Double? = null,

    @field:NotNull(message = "El valor es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "El valor no puede ser negativo")
    val value: Double,

    @field:NotNull(message = "La moneda es obligatoria")
    val currency: Currency,

    val change: Double = 0.0
)

data class UpdateHoldingRequest(
    val name: String? = null,
    val cls: AssetClass? = null,
    val platform: String? = null,
    val qty: Double? = null,
    val value: Double? = null,
    val currency: Currency? = null,
    val change: Double? = null
)
