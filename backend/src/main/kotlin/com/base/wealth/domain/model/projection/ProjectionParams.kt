package com.base.wealth.domain.model.projection

import com.base.wealth.domain.model.Money
import java.math.BigDecimal

/** CA-07.6: the same range the controller's `@Validated` query params enforce — this is the defense-in-depth copy. */
data class ProjectionParams private constructor(
    val principal: Money,
    val monthlyContribution: Money,
    val annualYieldPct: BigDecimal,
    val years: Int,
    val milestones: List<Money>,
) {
    companion object {
        const val MAX_YEARS = 50
        const val MAX_MILESTONES = 5
        private val MAX_YIELD_PCT = BigDecimal(100)

        fun of(
            principal: Money,
            monthlyContribution: Money,
            annualYieldPct: BigDecimal,
            years: Int,
            milestones: List<Money>,
        ): ProjectionParams {
            require(years in 1..MAX_YEARS) { "years must be between 1 and $MAX_YEARS" }
            require(annualYieldPct.signum() >= 0 && annualYieldPct <= MAX_YIELD_PCT) {
                "yieldPct must be between 0 and 100"
            }
            require(milestones.size <= MAX_MILESTONES) { "at most $MAX_MILESTONES milestones" }
            return ProjectionParams(principal, monthlyContribution, annualYieldPct, years, milestones.sorted())
        }
    }
}
