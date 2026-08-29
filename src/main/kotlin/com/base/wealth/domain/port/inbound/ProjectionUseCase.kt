package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.model.projection.Milestone
import com.base.wealth.domain.model.projection.ProjectionPoint
import java.math.BigDecimal

data class ProjectionRequest(
    val userId: UserId,
    val monthlyContribution: Double,
    val annualYieldPct: Double,
    val years: Int,
    val milestones: List<Double>,
    /** Overrides the user's current net worth as the projection's starting point (what-if simulations). */
    val principalOverride: Double? = null,
)

data class ProjectionResult(
    val principal: Money,
    val monthlyContribution: Money,
    val annualYieldPct: BigDecimal,
    val years: Int,
    val series: List<ProjectionPoint>,
    val milestones: List<Milestone>,
)

interface ProjectionUseCase {
    fun project(request: ProjectionRequest): ProjectionResult
}
