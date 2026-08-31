package com.base.wealth.application.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.AssetClassAggregate
import com.base.wealth.domain.port.outbound.PlatformAggregate
import com.base.wealth.domain.port.outbound.SnapshotRepository
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.util.UUID

class WealthQueryServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val wealthAggregationPort = mockk<WealthAggregationPort>()
    private val snapshotRepository = mockk<SnapshotRepository>()
    private val clock = mockk<Clock>()

    @BeforeEach
    fun setUp() {
        every { clock.instant() } returns Instant.parse("2026-06-15T00:00:00Z")
    }

    private fun service(defaultFxUsdArs: Double) =
        WealthQueryService(
            wealthAggregationPort,
            snapshotRepository,
            clock,
            listOf("Cash", "Equity"),
            defaultFxUsdArs,
        )

    @Test
    @DisplayName("empty portfolio: zeros everywhere, no NaN, no division by zero (CA-05.4)")
    fun emptyPortfolio() {
        every { wealthAggregationPort.netWorth(userId) } returns Money.ZERO
        every { wealthAggregationPort.byAssetClass(userId) } returns emptyList()
        every { wealthAggregationPort.byPlatform(userId) } returns emptyList()
        every { snapshotRepository.findFirstOfYear(userId, 2026) } returns null
        every { snapshotRepository.findEarliest(userId) } returns null

        val summary = service(defaultFxUsdArs = 0.0).getSummary(userId)

        assertEquals(0.0, summary.netWorth.usd)
        assertNull(summary.netWorth.ars)
        assertEquals(0, summary.holdingsCount)
        assertEquals("NO_BASELINE", summary.ytd.basis)
        assertEquals(0.0, summary.ytd.growthPct)
        assertEquals(0.0, summary.liquidity.liquidPct)
        assertEquals(emptyList<Any>(), summary.byAssetClass)
        assertEquals(emptyList<Any>(), summary.byPlatform)
    }

    @Test
    @DisplayName("byAssetClass percentages are computed against total net worth and FX is applied to ARS")
    fun populatedPortfolio() {
        val cash = AssetClass.of("Cash")
        val realEstate = AssetClass.of("Real Estate")
        every { wealthAggregationPort.netWorth(userId) } returns Money.of(1000.0)
        every { wealthAggregationPort.byAssetClass(userId) } returns
            listOf(
                AssetClassAggregate(cash, Money.of(750.0), 2),
                AssetClassAggregate(realEstate, Money.of(250.0), 1),
            )
        every { wealthAggregationPort.byPlatform(userId) } returns
            listOf(PlatformAggregate(PlatformName.of("Balanz"), PlatformType.of("Broker"), Money.of(1000.0), 3))
        every { snapshotRepository.findFirstOfYear(userId, 2026) } returns null
        every { snapshotRepository.findEarliest(userId) } returns null

        val summary = service(defaultFxUsdArs = 1000.0).getSummary(userId)

        assertEquals(1000.0, summary.netWorth.usd)
        assertEquals(1000000.0, summary.netWorth.ars)
        assertEquals(3, summary.holdingsCount)
        assertEquals(75.0, summary.byAssetClass.first { it.assetClass == "Cash" }.pct)
        assertEquals(25.0, summary.byAssetClass.first { it.assetClass == "Real Estate" }.pct)
        // "Real Estate" isn't in the configured liquid set (Cash, Equity) — counts as illiquid (CA-05.5).
        assertEquals(75.0, summary.liquidity.liquidPct)
    }
}
