package com.base.wealth.domain.service

import com.base.wealth.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Golden cases (CA-07.3, tasks.md T-31): same formula and inputs frontend/src/lib/calculations.ts
 * (fvYears/monthsToReach) and the old application.service.WealthCalculationService use — this is
 * what proves the Fase 5 migration doesn't silently change anyone's numbers. Tolerance 0.01 as
 * specified; the r=0 and large-years cases assert exactly since they're not compounding-sensitive.
 */
class CompoundInterestCalculatorTest {
    private fun money(v: Double) = Money.of(v)

    @Test
    @DisplayName("r=0: future value is just principal + contributions, no compounding")
    fun zeroYield() {
        val fv = CompoundInterestCalculator.futureValue(money(10000.0), money(500.0), BigDecimal.ZERO, 60)
        assertEquals(40000.0, fv.amount.toDouble(), 0.01)
    }

    @Test
    @DisplayName("positive yield over 10 years matches frontend/src/lib/calculations.ts::fvYears within 0.01 (CA-07.3)")
    fun positiveYieldTenYears() {
        val fv = CompoundInterestCalculator.futureValue(money(50000.0), money(1000.0), BigDecimal("10.0"), 120)
        assertEquals(340197.05, fv.amount.toDouble(), 0.01)
    }

    @Test
    @DisplayName("years=1, 9% yield — the frontend's default estimate scenario, exact to 0.01 (CA-07.3)")
    fun oneYear() {
        val fv = CompoundInterestCalculator.futureValue(money(84250.0), money(900.0), BigDecimal("9.0"), 12)
        assertEquals(103410.06, fv.amount.toDouble(), 0.01)
    }

    @Test
    @DisplayName("years=50 (600 months) matches the frontend formula within 0.01, no overflow (CA-07.3)")
    fun fiftyYears() {
        val fv = CompoundInterestCalculator.futureValue(money(10000.0), money(200.0), BigDecimal("7.0"), 600)
        assertEquals(1417418.32, fv.amount.toDouble(), 0.01)
    }

    @Test
    @DisplayName("principal=0, contribution=0: stays at zero forever")
    fun zeroEverything() {
        val fv = CompoundInterestCalculator.futureValue(Money.ZERO, Money.ZERO, BigDecimal("8.0"), 120)
        assertEquals(Money.ZERO, fv)
    }

    @Test
    @DisplayName("a milestone already reached returns month 0")
    fun milestoneAlreadyAchieved() {
        val months =
            CompoundInterestCalculator.monthsToReach(
                money(100.0),
                money(200.0),
                money(0.0),
                BigDecimal.ZERO,
                600,
            )
        assertEquals(0, months)
    }

    @Test
    @DisplayName("an unreachable milestone within the horizon returns null, not an infinite loop")
    fun milestoneUnreachable() {
        val months =
            CompoundInterestCalculator.monthsToReach(
                money(1_000_000.0),
                money(0.0),
                money(0.0),
                BigDecimal.ZERO,
                12,
            )
        assertNull(months)
    }

    @Test
    @DisplayName("a reachable milestone lands on the exact month the frontend formula predicts (CA-07.3)")
    fun milestoneReachableWithinAYear() {
        val months =
            CompoundInterestCalculator.monthsToReach(
                money(100000.0),
                money(80000.0),
                money(2000.0),
                BigDecimal("8.0"),
                600,
            )
        assertEquals(8, months)
    }
}
