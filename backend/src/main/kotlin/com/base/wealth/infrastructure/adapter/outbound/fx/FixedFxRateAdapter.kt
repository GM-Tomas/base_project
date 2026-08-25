package com.base.wealth.infrastructure.adapter.outbound.fx

import com.base.wealth.domain.model.FxRate
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.FxRatePort
import com.base.wealth.infrastructure.config.WealthProperties
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Default FxRatePort adapter: reads a fixed rate from config (D4).
 * Swap for a live-rate adapter without touching callers.
 */
@Component
class FixedFxRateAdapter(
    private val wealthProperties: WealthProperties,
    private val clock: ClockPort,
) : FxRatePort {
    override fun current(): FxRate {
        val rate = BigDecimal.valueOf(wealthProperties.defaultFxUsdArs)
        if (rate.signum() <= 0) return FxRate.Unavailable
        return FxRate.Known(rate, clock.now())
    }
}
