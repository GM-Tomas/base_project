package com.base.wealth.domain.model

import java.math.BigDecimal
import java.time.Instant

enum class YtdBasis { YEAR_START_SNAPSHOT, EARLIEST_SNAPSHOT }

/** Null Object for "no YTD baseline yet" (CA-05.7) — callers branch on the sealed type, never on a nullable. */
sealed interface YtdGrowth {
    data class From(
        val basis: YtdBasis,
        val baselineValue: Money,
        val baselineAt: Instant,
        val growthPct: BigDecimal,
    ) : YtdGrowth

    data object NoBaseline : YtdGrowth
}
