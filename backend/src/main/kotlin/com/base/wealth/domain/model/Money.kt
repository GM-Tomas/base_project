package com.base.wealth.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * A non-negative USD amount, always scale 2. Replaces `Double` for money (NFR-8) — binary floats
 * can't represent cents exactly, and this project sums many small holdings into totals people
 * read as ground truth.
 */
@JvmInline
value class Money private constructor(
    val amount: BigDecimal,
) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(amount + other.amount)

    operator fun minus(other: Money): Money = of((amount - other.amount).coerceAtLeast(BigDecimal.ZERO))

    operator fun times(factor: BigDecimal): Money = of((amount * factor).setScale(2, RoundingMode.HALF_UP))

    override operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    /**
     * Percentage of [total] this amount represents, 1 decimal.
     * `null` when [total] is zero — there's no share of nothing.
     */
    fun percentOf(total: Money): BigDecimal? {
        if (total.amount.signum() == 0) return null
        return amount
            .multiply(HUNDRED)
            .divide(total.amount, 1, RoundingMode.HALF_UP)
    }

    /**
     * Percentage growth from [baseline] to this amount, 1 decimal. `null` when [baseline] is
     * zero — there's no growth rate from nothing (YTD and snapshot-to-snapshot deltas, CA-05.7/CA-06.3).
     */
    fun growthPctFrom(baseline: Money): BigDecimal? {
        if (baseline.amount.signum() == 0) return null
        return amount
            .subtract(baseline.amount)
            .multiply(HUNDRED)
            .divide(baseline.amount, 1, RoundingMode.HALF_UP)
    }

    companion object {
        private val HUNDRED = BigDecimal(100)
        val ZERO = Money(BigDecimal.ZERO.setScale(2))

        fun of(amount: BigDecimal): Money {
            require(amount.signum() >= 0) { "Money must not be negative: $amount" }
            return Money(amount.setScale(2, RoundingMode.HALF_UP))
        }

        fun of(amount: Double): Money = of(BigDecimal.valueOf(amount))

        fun sum(values: Iterable<Money>): Money = values.fold(ZERO) { acc, m -> acc + m }
    }
}
