package com.base.wealth.domain.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.SnapshotId
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.model.YtdBasis
import com.base.wealth.domain.model.YtdGrowth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class YtdGrowthCalculatorTest {
    private val userId = UserId(UUID.randomUUID())

    private fun snapshotOf(
        value: Double,
        at: Instant,
    ) = NetWorthSnapshot(SnapshotId.new(), userId, at, Money.of(value))

    @Test
    @DisplayName("prefers the year-start snapshot over the earliest one (CA-05.7)")
    fun prefersYearStartBaseline() {
        val yearStart = snapshotOf(1000.0, Instant.parse("2026-01-01T00:00:00Z"))
        val earliest = snapshotOf(500.0, Instant.parse("2024-01-01T00:00:00Z"))

        val result = YtdGrowthCalculator.calculate(Money.of(1100.0), yearStart, earliest)

        assertTrue(result is YtdGrowth.From)
        result as YtdGrowth.From
        assertEquals(YtdBasis.YEAR_START_SNAPSHOT, result.basis)
        assertEquals(BigDecimal("10.0"), result.growthPct)
    }

    @Test
    @DisplayName("falls back to the earliest snapshot when there's none for the current year")
    fun fallsBackToEarliest() {
        val earliest = snapshotOf(500.0, Instant.parse("2024-01-01T00:00:00Z"))

        val result = YtdGrowthCalculator.calculate(Money.of(600.0), null, earliest)

        assertTrue(result is YtdGrowth.From)
        result as YtdGrowth.From
        assertEquals(YtdBasis.EARLIEST_SNAPSHOT, result.basis)
        assertEquals(BigDecimal("20.0"), result.growthPct)
    }

    @Test
    @DisplayName("no snapshots at all: NoBaseline, not a division by zero (CA-05.7)")
    fun noBaselineWhenNoHistory() {
        assertEquals(YtdGrowth.NoBaseline, YtdGrowthCalculator.calculate(Money.of(600.0), null, null))
    }

    @Test
    @DisplayName("a zero-value baseline is treated as NoBaseline, not an infinite growth rate")
    fun noBaselineWhenBaselineIsZero() {
        val zeroBaseline = snapshotOf(0.0, Instant.parse("2026-01-01T00:00:00Z"))
        assertEquals(YtdGrowth.NoBaseline, YtdGrowthCalculator.calculate(Money.of(600.0), zeroBaseline, null))
    }
}
