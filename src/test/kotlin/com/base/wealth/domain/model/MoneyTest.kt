package com.base.wealth.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MoneyTest {
    @Test
    @DisplayName("rounds to 2 decimals, half up")
    fun roundsHalfUp() {
        assertEquals(BigDecimal("10.13"), Money.of(BigDecimal("10.125")).amount)
    }

    @Test
    @DisplayName("33.33 x 3 does not leave a phantom cent — the classic Double failure case")
    fun noPhantomCents() {
        val third = Money.of(BigDecimal("33.33"))
        val total = third + third + third
        assertEquals(BigDecimal("99.99"), total.amount)
    }

    @Test
    @DisplayName("0.1 + 0.2 is exact in BigDecimal, unlike Double")
    fun noBinaryFloatDrift() {
        assertEquals(BigDecimal("0.30"), (Money.of(BigDecimal("0.1")) + Money.of(BigDecimal("0.2"))).amount)
    }

    @Test
    @DisplayName("negative amounts are rejected at construction")
    fun rejectsNegative() {
        assertThrows(IllegalArgumentException::class.java) { Money.of(BigDecimal("-0.01")) }
    }

    @Test
    @DisplayName("percentOf a zero total is null, not a division by zero (CA-05.4)")
    fun percentOfZeroTotalIsNull() {
        assertNull(Money.of(BigDecimal("50")).percentOf(Money.ZERO))
    }

    @Test
    @DisplayName("percentOf a positive total rounds to 1 decimal")
    fun percentOfRounds() {
        val pct = Money.of(BigDecimal("33.333")).percentOf(Money.of(BigDecimal("100")))
        assertEquals(BigDecimal("33.3"), pct)
    }

    @Test
    @DisplayName("sum of an empty collection is zero, not an exception")
    fun sumOfEmptyIsZero() {
        assertEquals(Money.ZERO, Money.sum(emptyList()))
    }

    @Test
    @DisplayName("minus never goes below zero — Money can't represent a negative balance")
    fun minusCoercesAtZero() {
        val result = Money.of(BigDecimal("5")) - Money.of(BigDecimal("10"))
        assertEquals(Money.ZERO, result)
    }

    @Test
    @DisplayName("growthPctFrom a zero baseline is null, not an infinite growth rate (CA-05.7)")
    fun growthPctFromZeroBaselineIsNull() {
        assertNull(Money.of(BigDecimal("50")).growthPctFrom(Money.ZERO))
    }

    @Test
    @DisplayName("growthPctFrom computes a signed percentage change, rounded to 1 decimal")
    fun growthPctFromComputesSignedChange() {
        assertEquals(BigDecimal("10.0"), Money.of(BigDecimal("1100")).growthPctFrom(Money.of(BigDecimal("1000"))))
        assertEquals(BigDecimal("-10.0"), Money.of(BigDecimal("900")).growthPctFrom(Money.of(BigDecimal("1000"))))
    }
}
