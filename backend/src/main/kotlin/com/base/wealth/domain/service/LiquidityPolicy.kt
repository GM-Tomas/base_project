package com.base.wealth.domain.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Money
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Which asset classes count as liquid is server config (CA-05.6), not a hardcoded constant —
 * classes the caller didn't configure (including ones a user invented, F10) count as illiquid.
 */
class LiquidityPolicy(
    private val liquidAssetClasses: Set<AssetClass>,
) {
    data class Breakdown(
        val liquidPct: BigDecimal,
        val illiquidPct: BigDecimal,
    )

    /**
     * [valueByClass]: total value per asset class, e.g. from
     * [com.base.wealth.domain.port.outbound.WealthAggregationPort.byAssetClass].
     */
    fun breakdown(valueByClass: Map<AssetClass, Money>): Breakdown {
        val total = Money.sum(valueByClass.values)
        val liquidValue = Money.sum(valueByClass.filterKeys { it in liquidAssetClasses }.values)
        val liquidPct = liquidValue.percentOf(total) ?: BigDecimal.ZERO.setScale(1)
        val illiquidPct = HUNDRED.subtract(liquidPct).setScale(1, RoundingMode.HALF_UP)
        return Breakdown(liquidPct, illiquidPct)
    }

    private companion object {
        val HUNDRED: BigDecimal = BigDecimal(100).setScale(1)
    }
}
