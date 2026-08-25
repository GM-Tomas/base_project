package com.base.wealth.application.dto

data class ProjectionPointResponse(
    val year: Int,
    val futureValueUsd: Double,
    val totalContributedUsd: Double,
    val interestEarnedUsd: Double,
)

data class MilestoneResponse(
    val amountUsd: Double,
    val status: String,
    val monthsRequired: Int?,
    val targetMonth: String?,
)

data class ProjectionResponse(
    val principalUsd: Double,
    val monthlyContributionUsd: Double,
    val annualYieldPct: Double,
    val years: Int,
    val series: List<ProjectionPointResponse>,
    val milestones: List<MilestoneResponse>,
)
