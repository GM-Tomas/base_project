package com.base.wealth.domain.service

import com.base.wealth.domain.model.projection.Milestone
import com.base.wealth.domain.model.projection.MilestoneStatus
import com.base.wealth.domain.model.projection.ProjectionParams
import com.base.wealth.domain.model.projection.ProjectionPoint
import java.math.BigDecimal
import java.time.YearMonth

/** CA-07.1, CA-07.4, CA-07.5 — pure orchestration over [CompoundInterestCalculator]. */
object ProjectionCalculator {
    private const val MONTHS_PER_YEAR = 12

    /** `years + 1` points, year 0 through [ProjectionParams.years] — year 0 equals the principal (CA-07.1). */
    fun series(params: ProjectionParams): List<ProjectionPoint> =
        (0..params.years).map { year ->
            val months = year * MONTHS_PER_YEAR
            val futureValue =
                CompoundInterestCalculator.futureValue(
                    params.principal,
                    params.monthlyContribution,
                    params.annualYieldPct,
                    months,
                )
            val totalContributed = params.principal + params.monthlyContribution * BigDecimal(months)
            ProjectionPoint(year, futureValue, totalContributed, futureValue - totalContributed)
        }

    /**
     * Search horizon is `years·12` months (CA-07.5) — a milestone reachable beyond that is
     * [MilestoneStatus.OUT_OF_HORIZON].
     */
    fun milestones(
        params: ProjectionParams,
        now: YearMonth,
    ): List<Milestone> {
        val maxMonths = params.years * MONTHS_PER_YEAR
        return params.milestones.map { amount ->
            val monthsRequired =
                CompoundInterestCalculator.monthsToReach(
                    amount,
                    params.principal,
                    params.monthlyContribution,
                    params.annualYieldPct,
                    maxMonths,
                )
            when (monthsRequired) {
                null -> Milestone(amount, MilestoneStatus.OUT_OF_HORIZON, null, null)
                0 -> Milestone(amount, MilestoneStatus.ACHIEVED, 0, null)
                else ->
                    Milestone(
                        amount,
                        MilestoneStatus.REACHABLE,
                        monthsRequired,
                        now.plusMonths(monthsRequired.toLong()),
                    )
            }
        }
    }
}
