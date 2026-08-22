package com.base.wealth.application.service

import com.base.wealth.application.dto.EstimateRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.math.roundToLong

class WealthCalculationServiceTest {

    private lateinit var service: WealthCalculationService

    @BeforeEach
    fun setUp() {
        service = WealthCalculationService()
    }

    @Test
    @DisplayName("Debe calcular el valor futuro con rendimiento cero correctamente")
    fun testZeroYieldFutureValue() {
        val principal = 10000.0
        val monthlyContribution = 500.0
        val yieldPct = 0.0
        val years = 5

        val fv = service.calculateFutureValueYears(principal, monthlyContribution, yieldPct, years)
        val expected = principal + (monthlyContribution * years * 12)

        assertEquals(expected, fv, 0.01)
    }

    @Test
    @DisplayName("Debe calcular el interés compuesto con rendimiento positivo")
    fun testPositiveYieldFutureValue() {
        val principal = 50000.0
        val monthlyContribution = 1000.0
        val yieldPct = 10.0 // 10% anual
        val years = 10

        val fv = service.calculateFutureValueYears(principal, monthlyContribution, yieldPct, years)

        // El valor futuro debe ser significativamente mayor a la suma lineal de aportes
        val totalInvested = principal + (monthlyContribution * years * 12)
        assertTrue(fv > totalInvested)
        assertEquals(339893.0, (fv).roundToLong().toDouble(), 500.0)
    }

    @Test
    @DisplayName("Debe calcular correctamente los meses requeridos para alcanzar un objetivo (milestone)")
    fun testMonthsToReachMilestone() {
        val principal = 80000.0
        val threshold = 100000.0
        val monthly = 2000.0
        val yieldPct = 8.0

        val months = service.monthsToReach(threshold, principal, monthly, yieldPct)

        assertNotNull(months)
        assertTrue(months!! > 0 && months <= 12)
    }

    @Test
    @DisplayName("Debe generar la proyección completa con desglose anual e hitos")
    fun testGenerateEstimate() {
        val request = EstimateRequest(
            contribution = 1500.0,
            yieldPct = 8.5,
            years = 10,
            initialPrincipal = 84250.0
        )

        val response = service.generateEstimate(request, 84250.0)

        assertEquals(84250.0, response.initialPrincipal)
        assertEquals(1500.0, response.monthlyContribution)
        assertEquals(10, response.totalYears)
        assertEquals(10, response.yearlyBreakdown.size)
        assertTrue(response.totalFutureValue > response.totalInvested)
        assertTrue(response.milestones.isNotEmpty())
    }
}
