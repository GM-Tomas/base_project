package com.base.wealth.domain.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class LiquidityPolicyTest {
    private val cash = AssetClass.of("Cash")
    private val equity = AssetClass.of("Equity")
    private val realEstate = AssetClass.of("Real Estate")
    private val policy = LiquidityPolicy(setOf(cash, equity))

    @Test
    @DisplayName("a class outside the configured liquid set counts as illiquid, even one the user invented")
    fun unknownClassIsIlliquid() {
        val breakdown = policy.breakdown(mapOf(cash to Money.of(50.0), realEstate to Money.of(50.0)))
        assertEquals(BigDecimal("50.0"), breakdown.liquidPct)
        assertEquals(BigDecimal("50.0"), breakdown.illiquidPct)
    }

    @Test
    @DisplayName("all liquid holdings: 100/0, not 100/0.0 rounding artifacts")
    fun allLiquid() {
        val breakdown = policy.breakdown(mapOf(cash to Money.of(100.0), equity to Money.of(50.0)))
        assertEquals(BigDecimal("100.0"), breakdown.liquidPct)
        assertEquals(BigDecimal("0.0"), breakdown.illiquidPct)
    }

    @Test
    @DisplayName("empty portfolio: 0/100, no division by zero (CA-05.4)")
    fun emptyPortfolio() {
        val breakdown = policy.breakdown(emptyMap())
        assertEquals(BigDecimal("0.0"), breakdown.liquidPct)
        assertEquals(BigDecimal("100.0"), breakdown.illiquidPct)
    }
}
