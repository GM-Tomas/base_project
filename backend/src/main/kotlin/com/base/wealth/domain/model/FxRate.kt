package com.base.wealth.domain.model

import java.math.BigDecimal
import java.time.Instant

/** Null Object for "no FX rate available" (D4) — callers branch on the sealed type, never on a nullable. */
sealed interface FxRate {
    data class Known(
        val rate: BigDecimal,
        val asOf: Instant,
    ) : FxRate {
        init {
            require(rate.signum() > 0) { "FX rate must be positive: $rate" }
        }
    }

    data object Unavailable : FxRate
}
