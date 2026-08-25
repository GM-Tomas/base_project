package com.base.wealth.domain.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.projection.MilestoneStatus
import com.base.wealth.domain.model.projection.ProjectionParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.YearMonth

class ProjectionCalculatorTest {
    @Test
    @DisplayName("series has years+1 points, year 0 equal to the principal (CA-07.1)")
    fun seriesHasYearsPlusOnePoints() {
        val params = ProjectionParams.of(Money.of(84250.0), Money.of(900.0), BigDecimal("9.0"), 12, emptyList())

        val series = ProjectionCalculator.series(params)

        assertEquals(13, series.size)
        assertEquals(0, series[0].year)
        assertEquals(Money.of(84250.0), series[0].futureValue)
        assertEquals(Money.of(84250.0), series[0].totalContributed)
        assertEquals(Money.ZERO, series[0].interestEarned)
        assertEquals(12, series[12].year)
    }

    @Test
    @DisplayName("a milestone already at or below the principal is ACHIEVED with month 0 and no target month")
    fun milestoneAlreadyAchieved() {
        val params =
            ProjectionParams.of(
                Money.of(200000.0),
                Money.of(0.0),
                BigDecimal.ZERO,
                5,
                listOf(Money.of(150000.0)),
            )

        val milestone = ProjectionCalculator.milestones(params, YearMonth.of(2026, 8)).single()

        assertEquals(MilestoneStatus.ACHIEVED, milestone.status)
        assertEquals(0, milestone.monthsRequired)
        assertEquals(null, milestone.targetMonth)
    }

    @Test
    @DisplayName("a milestone reachable within the horizon is REACHABLE with a computed target month (CA-07.4)")
    fun milestoneReachableWithinHorizon() {
        val params =
            ProjectionParams.of(
                Money.of(84250.0),
                Money.of(900.0),
                BigDecimal("9.0"),
                12,
                listOf(Money.of(150000.0)),
            )

        val milestone = ProjectionCalculator.milestones(params, YearMonth.of(2026, 8)).single()

        assertEquals(MilestoneStatus.REACHABLE, milestone.status)
        assertEquals(38, milestone.monthsRequired)
        assertEquals(YearMonth.of(2029, 10), milestone.targetMonth)
    }

    @Test
    @DisplayName("a milestone beyond years*12 months is OUT_OF_HORIZON, not a far-future date (CA-07.5)")
    fun milestoneOutOfHorizon() {
        val params =
            ProjectionParams.of(
                Money.of(1000.0),
                Money.of(10.0),
                BigDecimal("1.0"),
                1,
                listOf(Money.of(1_000_000.0)),
            )

        val milestone = ProjectionCalculator.milestones(params, YearMonth.of(2026, 8)).single()

        assertEquals(MilestoneStatus.OUT_OF_HORIZON, milestone.status)
        assertEquals(null, milestone.monthsRequired)
        assertEquals(null, milestone.targetMonth)
    }
}
