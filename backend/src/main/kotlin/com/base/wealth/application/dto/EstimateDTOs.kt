package com.base.wealth.application.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class EstimateRequest(
    @field:NotNull(message = "El aporte mensual es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "El aporte mensual no puede ser negativo")
    val contribution: Double,

    @field:NotNull(message = "El rendimiento estimado es obligatorio")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "El rendimiento anual no puede ser negativo")
    val yieldPct: Double,

    @field:NotNull(message = "Los años de horizonte son obligatorios")
    @field:Min(value = 1, message = "El horizonte mínimo es de 1 año")
    @field:Max(value = 50, message = "El horizonte máximo es de 50 años")
    val years: Int,

    val initialPrincipal: Double? = null
)

data class YearlyProjection(
    val year: Int,
    val totalInvested: Double,
    val futureValue: Double,
    val interestEarned: Double
)

data class MilestoneProjection(
    val milestoneAmount: Double,
    val monthsRequired: Int?,
    val targetDateLabel: String?,
    val isAchieved: Boolean
)

data class EstimateResponse(
    val initialPrincipal: Double,
    val monthlyContribution: Double,
    val annualYieldPct: Double,
    val totalYears: Int,
    val totalInvested: Double,
    val totalFutureValue: Double,
    val totalInterestEarned: Double,
    val yearlyBreakdown: List<YearlyProjection>,
    val milestones: List<MilestoneProjection>
)
