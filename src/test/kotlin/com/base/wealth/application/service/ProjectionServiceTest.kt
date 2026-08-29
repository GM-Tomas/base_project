package com.base.wealth.application.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.ProjectionRequest
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ProjectionServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val wealthAggregationPort = mockk<WealthAggregationPort>()
    private val clock = mockk<ClockPort>()
    private val service = ProjectionService(wealthAggregationPort, clock)

    @Test
    @DisplayName("principal defaults to the user's current net worth when no override is given")
    fun defaultsPrincipalToCurrentNetWorth() {
        every { clock.now() } returns Instant.parse("2026-08-15T00:00:00Z")
        every { wealthAggregationPort.netWorth(userId) } returns Money.of(84250.0)

        val result =
            service.project(
                ProjectionRequest(
                    userId,
                    monthlyContribution = 900.0,
                    annualYieldPct = 9.0,
                    years = 1,
                    milestones = emptyList(),
                ),
            )

        assertEquals(Money.of(84250.0), result.principal)
        verify(exactly = 1) { wealthAggregationPort.netWorth(userId) }
    }

    @Test
    @DisplayName("an explicit principal override skips the net worth lookup entirely (what-if simulations)")
    fun principalOverrideSkipsNetWorthLookup() {
        every { clock.now() } returns Instant.parse("2026-08-15T00:00:00Z")

        val result =
            service.project(
                ProjectionRequest(
                    userId,
                    monthlyContribution = 900.0,
                    annualYieldPct = 9.0,
                    years = 1,
                    milestones = emptyList(),
                    principalOverride = 50000.0,
                ),
            )

        assertEquals(Money.of(50000.0), result.principal)
        verify(exactly = 0) { wealthAggregationPort.netWorth(any()) }
    }

    @Test
    @DisplayName("series has years+1 points and milestones are computed against the resolved principal")
    fun producesSeriesAndMilestones() {
        every { clock.now() } returns Instant.parse("2026-08-15T00:00:00Z")
        every { wealthAggregationPort.netWorth(userId) } returns Money.of(84250.0)

        val result =
            service.project(
                ProjectionRequest(
                    userId,
                    monthlyContribution = 900.0,
                    annualYieldPct = 9.0,
                    years = 1,
                    milestones = listOf(90000.0),
                ),
            )

        assertEquals(2, result.series.size)
        assertEquals(1, result.milestones.size)
    }
}
