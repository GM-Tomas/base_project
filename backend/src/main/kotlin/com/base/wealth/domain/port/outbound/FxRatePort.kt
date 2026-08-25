package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.FxRate

/**
 * Strategy (D4) — one adapter today (config-fixed), swappable for a live-rate source later
 * without touching callers.
 */
interface FxRatePort {
    fun current(): FxRate
}
