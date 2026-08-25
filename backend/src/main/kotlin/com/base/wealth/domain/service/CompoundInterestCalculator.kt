package com.base.wealth.domain.service

import com.base.wealth.domain.model.Money
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.pow

/**
 * Migrated from the old application.service.WealthCalculationService (tasks.md T-32/T-65) —
 * same formula, verified against the same golden cases the frontend's lib/calculations.ts uses
 * (CA-07.3). Internally Double, not Money/BigDecimal: this is a projection, not a ledger balance
 * — NFR-8 is about summed holdings, not an inherently-approximate what-if estimate.
 */
object CompoundInterestCalculator {
    private const val MONTHS_PER_YEAR = 12
    private const val PERCENT = 100.0
    private const val NEAR_ZERO_RATE = 1e-9

    /** FV = P*(1+r)^n + PMT*((1+r)^n - 1)/r, r = annualYieldPct/100/12. */
    fun futureValue(
        principal: Money,
        monthlyContribution: Money,
        annualYieldPct: BigDecimal,
        months: Int,
    ): Money {
        val p = principal.amount.toDouble()
        val pmt = monthlyContribution.amount.toDouble()
        val r = annualYieldPct.toDouble() / PERCENT / MONTHS_PER_YEAR
        val fv =
            if (abs(r) < NEAR_ZERO_RATE) {
                p + pmt * months
            } else {
                val compound = (1.0 + r).pow(months)
                p * compound + pmt * ((compound - 1.0) / r)
            }
        return Money.of(fv.coerceAtLeast(0.0))
    }

    /** First month (0..maxMonths) at which [futureValue] reaches [threshold], or `null` if it never does. */
    fun monthsToReach(
        threshold: Money,
        principal: Money,
        monthlyContribution: Money,
        annualYieldPct: BigDecimal,
        maxMonths: Int,
    ): Int? =
        (0..maxMonths).firstOrNull { month ->
            futureValue(principal, monthlyContribution, annualYieldPct, month) >= threshold
        }
}
