package com.base.wealth.domain.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.YtdBasis
import com.base.wealth.domain.model.YtdGrowth

/**
 * CA-05.7: baseline is the year's first snapshot; falls back to the earliest snapshot ever
 * captured; falls back to [YtdGrowth.NoBaseline] when there's no history or the baseline is 0.
 */
object YtdGrowthCalculator {
    fun calculate(
        currentNetWorth: Money,
        yearStartSnapshot: NetWorthSnapshot?,
        earliestSnapshot: NetWorthSnapshot?,
    ): YtdGrowth {
        val (basis, baseline) =
            when {
                yearStartSnapshot != null -> YtdBasis.YEAR_START_SNAPSHOT to yearStartSnapshot
                earliestSnapshot != null -> YtdBasis.EARLIEST_SNAPSHOT to earliestSnapshot
                else -> return YtdGrowth.NoBaseline
            }
        val growthPct = currentNetWorth.growthPctFrom(baseline.totalValue)
        return if (growthPct == null) {
            YtdGrowth.NoBaseline
        } else {
            YtdGrowth.From(basis, baseline.totalValue, baseline.capturedAt, growthPct)
        }
    }
}
