package com.base.wealth.application.dto

import java.time.Instant

data class FxRateDTO(
    val available: Boolean,
    val value: Double? = null,
    val asOf: Instant? = null,
    val source: String? = null,
)

data class NetWorthDTO(
    val usd: Double,
    val ars: Double?,
    val fxRate: FxRateDTO,
)

data class YtdDTO(
    val basis: String,
    val growthPct: Double,
    val baselineValueUsd: Double? = null,
    val baselineAt: Instant? = null,
)

data class LiquidityDTO(
    val liquidPct: Double,
    val illiquidPct: Double,
    val liquidAssetClasses: List<String>,
)

data class AssetClassBreakdown(
    val assetClass: String,
    val valueUsd: Double,
    val pct: Double,
    val count: Int,
)

data class PlatformBreakdown(
    val name: String,
    val type: String,
    val valueUsd: Double,
    val pct: Double,
    val count: Int,
)

data class WealthSummaryResponse(
    val netWorth: NetWorthDTO,
    val holdingsCount: Int,
    val ytd: YtdDTO,
    val liquidity: LiquidityDTO,
    val byAssetClass: List<AssetClassBreakdown>,
    val byPlatform: List<PlatformBreakdown>,
)
