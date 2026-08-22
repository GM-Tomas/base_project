package com.base.wealth.application.service

import com.base.wealth.application.dto.AssetClassBreakdown
import com.base.wealth.application.dto.EstimateRequest
import com.base.wealth.application.dto.EstimateResponse
import com.base.wealth.application.dto.PlatformBreakdown
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.domain.model.HistorySnapshot
import com.base.wealth.domain.port.inbound.HistoryUseCase
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.domain.port.inbound.PlatformUseCase
import com.base.wealth.domain.port.inbound.WealthUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.math.roundToLong

@Service
class WealthQueryService(
    private val holdingUseCase: HoldingUseCase,
    private val platformUseCase: PlatformUseCase,
    private val historyUseCase: HistoryUseCase,
    private val calculationService: WealthCalculationService,
    @Value("\${wealth.default-fx-usd-ars:1050.0}") private val fxRateUSDARS: Double
) : WealthUseCase {

    override fun getSummary(): WealthSummaryResponse {
        val holdings = holdingUseCase.getAllHoldings()
        val platforms = platformUseCase.getAllPlatforms().associateBy { it.name }

        val totalUSD = holdings.sumOf { it.value }
        val totalARS = totalUSD * fxRateUSDARS

        val avgChange = if (holdings.isNotEmpty()) {
            holdings.sumOf { it.change * it.value } / (if (totalUSD > 0) totalUSD else 1.0)
        } else {
            0.0
        }

        // Distribución por clase de activo
        val byClass = holdings.groupBy { it.cls }.map { (cls, list) ->
            val classVal = list.sumOf { it.value }
            val pct = if (totalUSD > 0) (classVal / totalUSD) * 100.0 else 0.0
            AssetClassBreakdown(
                cls = cls,
                totalValueUSD = (classVal * 100.0).roundToLong() / 100.0,
                percentage = (pct * 10.0).roundToLong() / 10.0,
                count = list.size
            )
        }.sortedByDescending { it.totalValueUSD }

        // Distribución por plataforma
        val byPlatform = holdings.groupBy { it.platform }.map { (platName, list) ->
            val platVal = list.sumOf { it.value }
            val pct = if (totalUSD > 0) (platVal / totalUSD) * 100.0 else 0.0
            PlatformBreakdown(
                platform = platName,
                type = platforms[platName]?.type,
                totalValueUSD = (platVal * 100.0).roundToLong() / 100.0,
                percentage = (pct * 10.0).roundToLong() / 10.0,
                count = list.size
            )
        }.sortedByDescending { it.totalValueUSD }

        return WealthSummaryResponse(
            totalNetWorthUSD = (totalUSD * 100.0).roundToLong() / 100.0,
            totalNetWorthARS = (totalARS * 100.0).roundToLong() / 100.0,
            fxRateUSDARS = fxRateUSDARS,
            average24hChangePct = (avgChange * 10.0).roundToLong() / 10.0,
            totalHoldingsCount = holdings.size,
            byAssetClass = byClass,
            byPlatform = byPlatform
        )
    }

    override fun calculateEstimate(request: EstimateRequest): EstimateResponse {
        val currentNetWorth = holdingUseCase.getTotalNetWorthUSD()
        return calculationService.generateEstimate(request, currentNetWorth)
    }

    override fun getHistory(): List<HistorySnapshot> = historyUseCase.getHistorySnapshots()
}
