package com.base.wealth.application.service

import com.base.wealth.application.dto.AssetClassBreakdown
import com.base.wealth.application.dto.FxRateDTO
import com.base.wealth.application.dto.LiquidityDTO
import com.base.wealth.application.dto.NetWorthDTO
import com.base.wealth.application.dto.PlatformBreakdown
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.application.dto.YtdDTO
import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.FxRate
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.model.YtdGrowth
import com.base.wealth.domain.port.inbound.WealthUseCase
import com.base.wealth.domain.port.outbound.AssetClassAggregate
import com.base.wealth.domain.port.outbound.PlatformAggregate
import com.base.wealth.domain.port.outbound.SnapshotRepository
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import com.base.wealth.domain.service.LiquidityPolicy
import com.base.wealth.domain.service.YtdGrowthCalculator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Clock
import java.time.ZoneOffset

private val ZERO_PCT: BigDecimal = BigDecimal.ZERO.setScale(1)

// ponytail: liquidAssetClasses and defaultFxUsdArs stay on @Value, not WealthProperties
// (infrastructure/config) — this service is `application`, and depending on an infrastructure
// class here is the same layer leak specs/001-backend-para-frontend/plan.md §8.2 checks for
// (see application/service/AssetClassService.kt).
@Service
class WealthQueryService(
    private val wealthAggregationPort: WealthAggregationPort,
    private val snapshotRepository: SnapshotRepository,
    private val clock: Clock,
    @Value("\${wealth.liquid-asset-classes:Cash,Equity,Crypto,Index Fund}")
    private val liquidAssetClasses: List<String>,
    @Value("\${wealth.default-fx-usd-ars:1050.0}")
    private val defaultFxUsdArs: Double,
) : WealthUseCase {
    override fun getSummary(userId: UserId): WealthSummaryResponse {
        val netWorth = wealthAggregationPort.netWorth(userId)
        val byClass = wealthAggregationPort.byAssetClass(userId)
        val byPlatform = wealthAggregationPort.byPlatform(userId)
        val year = clock.instant().atZone(ZoneOffset.UTC).year

        val ytd =
            YtdGrowthCalculator.calculate(
                netWorth,
                snapshotRepository.findFirstOfYear(userId, year),
                snapshotRepository.findEarliest(userId),
            )
        val liquidity =
            LiquidityPolicy(liquidAssetClasses.map(AssetClass::of).toSet())
                .breakdown(byClass.associate { it.assetClass to it.value })

        return WealthSummaryResponse(
            netWorth = netWorth.toDto(currentFxRate()),
            holdingsCount = byClass.sumOf { it.count },
            ytd = ytd.toDto(),
            liquidity =
                LiquidityDTO(
                    liquidity.liquidPct.toDouble(),
                    liquidity.illiquidPct.toDouble(),
                    liquidAssetClasses,
                ),
            byAssetClass = byClass.map { it.toDto(netWorth) },
            byPlatform = byPlatform.map { it.toDto(netWorth) },
        )
    }

    // ponytail: read straight from config (D4) — no live-rate source exists yet, add one (and a
    // `source` other than "FIXED_CONFIG" below) when a view actually needs it.
    private fun currentFxRate(): FxRate {
        val rate = BigDecimal.valueOf(defaultFxUsdArs)
        if (rate.signum() <= 0) return FxRate.Unavailable
        return FxRate.Known(rate, clock.instant())
    }

    private fun Money.toDto(fxRate: FxRate): NetWorthDTO =
        when (fxRate) {
            is FxRate.Known ->
                NetWorthDTO(
                    usd = amount.toDouble(),
                    ars = (this * fxRate.rate).amount.toDouble(),
                    fxRate =
                        FxRateDTO(
                            available = true,
                            value = fxRate.rate.toDouble(),
                            asOf = fxRate.asOf,
                            source = "FIXED_CONFIG",
                        ),
                )
            FxRate.Unavailable ->
                NetWorthDTO(usd = amount.toDouble(), ars = null, fxRate = FxRateDTO(available = false))
        }

    private fun YtdGrowth.toDto(): YtdDTO =
        when (this) {
            is YtdGrowth.From ->
                YtdDTO(
                    basis = basis.name,
                    growthPct = growthPct.toDouble(),
                    baselineValueUsd = baselineValue.amount.toDouble(),
                    baselineAt = baselineAt,
                )
            YtdGrowth.NoBaseline -> YtdDTO(basis = "NO_BASELINE", growthPct = 0.0)
        }

    private fun AssetClassAggregate.toDto(total: Money) =
        AssetClassBreakdown(
            assetClass = assetClass.value,
            valueUsd = value.amount.toDouble(),
            pct = (value.percentOf(total) ?: ZERO_PCT).toDouble(),
            count = count,
        )

    private fun PlatformAggregate.toDto(total: Money) =
        PlatformBreakdown(
            name = name.value,
            type = type.value,
            valueUsd = value.amount.toDouble(),
            pct = (value.percentOf(total) ?: ZERO_PCT).toDouble(),
            count = count,
        )
}
