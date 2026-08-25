package com.base.wealth.application.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.projection.ProjectionParams
import com.base.wealth.domain.port.inbound.ProjectionRequest
import com.base.wealth.domain.port.inbound.ProjectionResult
import com.base.wealth.domain.port.inbound.ProjectionUseCase
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import com.base.wealth.domain.service.ProjectionCalculator
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.time.ZoneOffset

@Service
class ProjectionService(
    private val wealthAggregationPort: WealthAggregationPort,
    private val clock: ClockPort,
) : ProjectionUseCase {
    override fun project(request: ProjectionRequest): ProjectionResult {
        val principal = request.principalOverride?.let(Money::of) ?: wealthAggregationPort.netWorth(request.userId)
        val params =
            ProjectionParams.of(
                principal = principal,
                monthlyContribution = Money.of(request.monthlyContribution),
                annualYieldPct = BigDecimal.valueOf(request.annualYieldPct),
                years = request.years,
                milestones = request.milestones.map(Money::of),
            )
        val now = YearMonth.from(clock.now().atZone(ZoneOffset.UTC))

        return ProjectionResult(
            principal = params.principal,
            monthlyContribution = params.monthlyContribution,
            annualYieldPct = params.annualYieldPct,
            years = params.years,
            series = ProjectionCalculator.series(params),
            milestones = ProjectionCalculator.milestones(params, now),
        )
    }
}
