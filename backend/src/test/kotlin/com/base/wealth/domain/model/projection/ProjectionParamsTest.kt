package com.base.wealth.domain.model.projection

import com.base.wealth.domain.model.Money
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProjectionParamsTest {
    private fun params(
        years: Int = 10,
        yieldPct: String = "8.0",
        milestones: List<Money> = emptyList(),
    ) = ProjectionParams.of(Money.of(1000.0), Money.of(100.0), BigDecimal(yieldPct), years, milestones)

    @Test
    @DisplayName("years outside [1, 50] is rejected (CA-07.6)")
    fun rejectsYearsOutOfRange() {
        assertThrows(IllegalArgumentException::class.java) { params(years = 0) }
        assertThrows(IllegalArgumentException::class.java) { params(years = 51) }
    }

    @Test
    @DisplayName("yieldPct outside [0, 100] is rejected (CA-07.6)")
    fun rejectsYieldOutOfRange() {
        assertThrows(IllegalArgumentException::class.java) { params(yieldPct = "-0.01") }
        assertThrows(IllegalArgumentException::class.java) { params(yieldPct = "100.01") }
    }

    @Test
    @DisplayName("more than 5 milestones is rejected (CA-07.4)")
    fun rejectsTooManyMilestones() {
        val sixMilestones = (1..6).map { Money.of(it * 1000.0) }
        assertThrows(IllegalArgumentException::class.java) { params(milestones = sixMilestones) }
    }

    @Test
    @DisplayName("milestones are sorted ascending regardless of input order")
    fun sortsMilestones() {
        val result = params(milestones = listOf(Money.of(500.0), Money.of(100.0), Money.of(300.0)))
        assertEquals(listOf(Money.of(100.0), Money.of(300.0), Money.of(500.0)), result.milestones)
    }
}
