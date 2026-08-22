package com.base.wealth.application.dto

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.PlatformType

data class AssetClassBreakdown(
    val cls: AssetClass,
    val totalValueUSD: Double,
    val percentage: Double,
    val count: Int
)

data class PlatformBreakdown(
    val platform: String,
    val type: PlatformType?,
    val totalValueUSD: Double,
    val percentage: Double,
    val count: Int
)

data class WealthSummaryResponse(
    val totalNetWorthUSD: Double,
    val totalNetWorthARS: Double,
    val fxRateUSDARS: Double,
    val average24hChangePct: Double,
    val totalHoldingsCount: Int,
    val byAssetClass: List<AssetClassBreakdown>,
    val byPlatform: List<PlatformBreakdown>
)
