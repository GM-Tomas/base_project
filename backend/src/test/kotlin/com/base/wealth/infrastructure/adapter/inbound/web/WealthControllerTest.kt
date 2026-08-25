package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.AssetClassBreakdown
import com.base.wealth.application.dto.FxRateDTO
import com.base.wealth.application.dto.LiquidityDTO
import com.base.wealth.application.dto.NetWorthDTO
import com.base.wealth.application.dto.PlatformBreakdown
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.application.dto.YtdDTO
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.SnapshotId
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.model.projection.Milestone
import com.base.wealth.domain.model.projection.MilestoneStatus
import com.base.wealth.domain.model.projection.ProjectionPoint
import com.base.wealth.domain.port.inbound.ProjectionResult
import com.base.wealth.domain.port.inbound.ProjectionUseCase
import com.base.wealth.domain.port.inbound.SnapshotUseCase
import com.base.wealth.domain.port.inbound.SnapshotWithChange
import com.base.wealth.domain.port.inbound.WealthUseCase
import com.base.wealth.exception.DuplicateResourceException
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAccessDeniedHandler
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAuthEntryPoint
import com.base.wealth.infrastructure.adapter.inbound.security.SecurityConfig
import com.base.wealth.infrastructure.config.WealthProperties
import com.base.wealth.support.authenticatedAs
import com.base.wealth.support.matchesContract
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// @Import(SecurityConfig): see HoldingControllerTest for why this is required, not optional.
@WebMvcTest(WealthController::class)
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class WealthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var wealthUseCase: WealthUseCase

    @MockkBean
    private lateinit var snapshotUseCase: SnapshotUseCase

    @MockkBean
    private lateinit var projectionUseCase: ProjectionUseCase

    @Test
    @DisplayName("GET /api/v1/wealth/summary debe retornar totales y agrupaciones")
    fun testGetSummary() {
        every { wealthUseCase.getSummary(any()) } returns
            WealthSummaryResponse(
                netWorth = NetWorthDTO(84250.0, 88462500.0, FxRateDTO(true, 1050.0, Instant.now(), "FIXED_CONFIG")),
                holdingsCount = 10,
                ytd = YtdDTO("EARLIEST_SNAPSHOT", 12.5, 74800.0, Instant.now()),
                liquidity = LiquidityDTO(62.5, 37.5, listOf("Cash", "Equity", "Crypto", "Index Fund")),
                byAssetClass = listOf(AssetClassBreakdown("Cash", 6150.0, 7.3, 1)),
                byPlatform = listOf(PlatformBreakdown("Balanz", "Broker", 32400.0, 38.5, 4)),
            )

        mockMvc
            .perform(get("/api/v1/wealth/summary").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.netWorth.usd").value(84250.0))
            .andExpect(jsonPath("$.netWorth.fxRate.available").value(true))
            .andExpect(jsonPath("$.ytd.basis").value("EARLIEST_SNAPSHOT"))
            .andExpect(jsonPath("$.liquidity.liquidPct").value(62.5))
            .andExpect(jsonPath("$.byAssetClass").isArray)
            .andExpect(jsonPath("$.byPlatform").isArray)
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("GET /api/v1/wealth/summary con cero holdings: sin NaN, sin 500 (CA-05.4)")
    fun testGetSummaryEmptyPortfolio() {
        every { wealthUseCase.getSummary(any()) } returns
            WealthSummaryResponse(
                netWorth = NetWorthDTO(0.0, 0.0, FxRateDTO(true, 1050.0, Instant.now(), "FIXED_CONFIG")),
                holdingsCount = 0,
                ytd = YtdDTO("NO_BASELINE", 0.0),
                liquidity = LiquidityDTO(0.0, 0.0, listOf("Cash", "Equity", "Crypto", "Index Fund")),
                byAssetClass = emptyList(),
                byPlatform = emptyList(),
            )

        mockMvc
            .perform(get("/api/v1/wealth/summary").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.netWorth.usd").value(0.0))
            .andExpect(jsonPath("$.ytd.basis").value("NO_BASELINE"))
            .andExpect(jsonPath("$.ytd.growthPct").value(0.0))
            .andExpect(jsonPath("$.liquidity.liquidPct").value(0.0))
            .andExpect(jsonPath("$.byAssetClass").isEmpty)
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("GET /api/v1/wealth/estimate debe calcular la proyección con years+1 puntos y hitos (CA-07.1)")
    fun testGetEstimate() {
        val series =
            listOf(
                ProjectionPoint(0, Money.of(84250.0), Money.of(84250.0), Money.ZERO),
                ProjectionPoint(1, Money.of(103410.06), Money.of(95050.0), Money.of(8360.06)),
            )
        val milestones =
            listOf(Milestone(Money.of(150000.0), MilestoneStatus.REACHABLE, 44, java.time.YearMonth.of(2030, 4)))
        every { projectionUseCase.project(any()) } returns
            ProjectionResult(Money.of(84250.0), Money.of(900.0), BigDecimal("9.0"), 1, series, milestones)

        mockMvc
            .perform(
                get("/api/v1/wealth/estimate")
                    .param("contribution", "900")
                    .param("yieldPct", "9")
                    .param("years", "1")
                    .with(authenticatedAs()),
            ).andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "max-age=30, private"))
            .andExpect(jsonPath("$.series.length()").value(2))
            .andExpect(jsonPath("$.series[0].futureValueUsd").value(84250.0))
            .andExpect(jsonPath("$.milestones[0].status").value("REACHABLE"))
            .andExpect(jsonPath("$.milestones[0].targetMonth").value("2030-04"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("GET /api/v1/wealth/estimate con years fuera de rango responde 400 (CA-07.6)")
    fun testGetEstimateValidatesYearsRange() {
        mockMvc
            .perform(
                get("/api/v1/wealth/estimate")
                    .param("contribution", "900")
                    .param("yieldPct", "9")
                    .param("years", "51")
                    .with(authenticatedAs()),
            ).andExpect(status().isBadRequest)
        // No matchesContract() here: the request is deliberately out of contract (years=51 >
        // the spec's own maximum) — that's the whole point of the test. Validating it would
        // just fail on the request half, which isn't the behavior under test.
    }

    @Test
    @DisplayName("GET /api/v1/wealth/snapshots debe retornar la serie con variación porcentual")
    fun testGetSnapshots() {
        val userId = UserId(UUID.randomUUID())
        val first = NetWorthSnapshot(SnapshotId.new(), userId, Instant.parse("2026-01-01T00:00:00Z"), Money.of(1000.0))
        val second = NetWorthSnapshot(SnapshotId.new(), userId, Instant.parse("2026-02-01T00:00:00Z"), Money.of(1100.0))
        every { snapshotUseCase.getSnapshots(any(), any(), any()) } returns
            listOf(SnapshotWithChange(first, null), SnapshotWithChange(second, BigDecimal("10.0")))

        mockMvc
            .perform(get("/api/v1/wealth/snapshots").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].changePctFromPrevious").doesNotExist())
            .andExpect(jsonPath("$[1].changePctFromPrevious").value(10.0))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("POST /api/v1/wealth/snapshots crea un snapshot con el importe calculado en el servidor")
    fun testCreateSnapshot() {
        val userId = UserId(UUID.randomUUID())
        val snapshot =
            NetWorthSnapshot(SnapshotId.new(), userId, Instant.parse("2026-03-01T12:00:00Z"), Money.of(5000.0))
        every { snapshotUseCase.createSnapshot(any()) } returns snapshot

        mockMvc
            .perform(post("/api/v1/wealth/snapshots").with(authenticatedAs()))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.totalValueUsd").value(5000.0))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("POST /api/v1/wealth/snapshots dos veces en el mismo segundo responde 409 (CA-06.4)")
    fun testCreateSnapshotConflict() {
        every { snapshotUseCase.createSnapshot(any()) } throws DuplicateResourceException("A snapshot already exists")

        mockMvc
            .perform(post("/api/v1/wealth/snapshots").with(authenticatedAs()))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(matchesContract())
    }
}
