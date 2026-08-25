package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.MilestoneResponse
import com.base.wealth.application.dto.ProjectionPointResponse
import com.base.wealth.application.dto.ProjectionResponse
import com.base.wealth.application.dto.SnapshotResponse
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.model.projection.Milestone
import com.base.wealth.domain.model.projection.ProjectionPoint
import com.base.wealth.domain.port.inbound.ProjectionRequest
import com.base.wealth.domain.port.inbound.ProjectionResult
import com.base.wealth.domain.port.inbound.ProjectionUseCase
import com.base.wealth.domain.port.inbound.SnapshotUseCase
import com.base.wealth.domain.port.inbound.SnapshotWithChange
import com.base.wealth.domain.port.inbound.WealthUseCase
import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.http.CacheControl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping("/api/v1/wealth")
@Validated
@Tag(name = "Wealth", description = "Endpoints de resumen patrimonial, proyecciones financieras e histórico")
class WealthController(
    private val wealthUseCase: WealthUseCase,
    private val snapshotUseCase: SnapshotUseCase,
    private val projectionUseCase: ProjectionUseCase,
) {
    companion object {
        private const val MAX_PROJECTION_YEARS = 50L
        private const val ESTIMATE_CACHE_MAX_AGE_SECONDS = 30L
        private val DEFAULT_MILESTONES = listOf(150_000.0, 250_000.0)
        private val ESTIMATE_CACHE =
            CacheControl.maxAge(Duration.ofSeconds(ESTIMATE_CACHE_MAX_AGE_SECONDS)).cachePrivate()
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtiene el resumen consolidado de patrimonio neto, métricas y distribuciones")
    fun getSummary(
        @CurrentUser userId: UUID,
    ): ResponseEntity<WealthSummaryResponse> = ResponseEntity.ok(wealthUseCase.getSummary(UserId(userId)))

    // CA-07.6: idempotent and cacheable — a pure function of the query params (plus the user's
    // current net worth), no state to mutate. GET, not POST.
    @GetMapping("/estimate")
    @Operation(summary = "Calcula la proyección de interés compuesto y el progreso de hitos financieros")
    fun getEstimate(
        @CurrentUser userId: UUID,
        @RequestParam @DecimalMin("0.0") @DecimalMax("1000000000.0") contribution: Double,
        @RequestParam @DecimalMin("0.0") @DecimalMax("100.0") yieldPct: Double,
        @RequestParam @Min(1) @Max(MAX_PROJECTION_YEARS) years: Int,
        @RequestParam(required = false) @Size(max = 5) milestones: List<
            @DecimalMin("0.0")
            Double,
        >?,
        @RequestParam(required = false) principal: Double?,
    ): ResponseEntity<ProjectionResponse> {
        val result =
            projectionUseCase.project(
                ProjectionRequest(
                    userId = UserId(userId),
                    monthlyContribution = contribution,
                    annualYieldPct = yieldPct,
                    years = years,
                    milestones = milestones ?: DEFAULT_MILESTONES,
                    principalOverride = principal,
                ),
            )
        return ResponseEntity.ok().cacheControl(ESTIMATE_CACHE).body(result.toResponse())
    }

    @GetMapping("/snapshots")
    @Operation(summary = "Serie histórica de patrimonio neto, con variación porcentual contra el punto anterior")
    fun getSnapshots(
        @CurrentUser userId: UUID,
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
    ): ResponseEntity<List<SnapshotResponse>> =
        ResponseEntity.ok(
            snapshotUseCase
                .getSnapshots(UserId(userId), from?.atStartOfDayUtc(), to?.atEndOfDayUtc())
                .map { it.toResponse() },
        )

    @PostMapping("/snapshots")
    @Operation(summary = "Captura un snapshot del patrimonio actual, calculado en el servidor")
    fun createSnapshot(
        @CurrentUser userId: UUID,
    ): ResponseEntity<SnapshotResponse> {
        val snapshot = snapshotUseCase.createSnapshot(UserId(userId))
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                SnapshotResponse(
                    snapshot.id.value.toString(),
                    snapshot.capturedAt,
                    snapshot.totalValue.amount.toDouble(),
                ),
            )
    }

    private fun LocalDate.atStartOfDayUtc() = atStartOfDay(ZoneOffset.UTC).toInstant()

    private fun LocalDate.atEndOfDayUtc() = atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant()

    private fun SnapshotWithChange.toResponse() =
        SnapshotResponse(
            id = snapshot.id.value.toString(),
            capturedAt = snapshot.capturedAt,
            totalValueUsd = snapshot.totalValue.amount.toDouble(),
            changePctFromPrevious = changePctFromPrevious?.toDouble(),
        )

    private fun ProjectionResult.toResponse() =
        ProjectionResponse(
            principalUsd = principal.amount.toDouble(),
            monthlyContributionUsd = monthlyContribution.amount.toDouble(),
            annualYieldPct = annualYieldPct.toDouble(),
            years = years,
            series = series.map { it.toResponse() },
            milestones = milestones.map { it.toResponse() },
        )

    private fun ProjectionPoint.toResponse() =
        ProjectionPointResponse(
            year = year,
            futureValueUsd = futureValue.amount.toDouble(),
            totalContributedUsd = totalContributed.amount.toDouble(),
            interestEarnedUsd = interestEarned.amount.toDouble(),
        )

    private fun Milestone.toResponse() =
        MilestoneResponse(
            amountUsd = amount.amount.toDouble(),
            status = status.name,
            monthsRequired = monthsRequired,
            targetMonth = targetMonth?.toString(),
        )
}
