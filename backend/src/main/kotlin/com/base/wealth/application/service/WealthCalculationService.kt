package com.base.wealth.application.service

import com.base.wealth.application.dto.EstimateRequest
import com.base.wealth.application.dto.EstimateResponse
import com.base.wealth.application.dto.MilestoneProjection
import com.base.wealth.application.dto.YearlyProjection
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

@Service
class WealthCalculationService {

    private val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    /**
     * Calcula el valor futuro dado un principal, aporte mensual, rendimiento anual y años.
     * Fórmula: P * (1 + r)^n + PMT * [((1 + r)^n - 1) / r]
     * donde r = annualPct / 100 / 12, n = years * 12
     */
    fun calculateFutureValueYears(principal: Double, monthlyPayment: Double, annualPct: Double, years: Int): Double {
        val r = annualPct / 100.0 / 12.0
        val n = years * 12.0
        if (abs(r) < 1e-9) {
            return principal + monthlyPayment * n
        }
        val compound = (1.0 + r).pow(n)
        return principal * compound + monthlyPayment * ((compound - 1.0) / r)
    }

    /**
     * Calcula el valor futuro dado un principal, aporte mensual, rendimiento anual y meses.
     */
    fun calculateFutureValueMonths(principal: Double, monthlyPayment: Double, annualPct: Double, months: Int): Double {
        val r = annualPct / 100.0 / 12.0
        val n = months.toDouble()
        if (abs(r) < 1e-9) {
            return principal + monthlyPayment * n
        }
        val compound = (1.0 + r).pow(n)
        return principal * compound + monthlyPayment * ((compound - 1.0) / r)
    }

    /**
     * Calcula los meses necesarios para alcanzar un determinado umbral de capital.
     */
    fun monthsToReach(threshold: Double, principal: Double, monthlyPayment: Double, annualPct: Double, maxMonths: Int = 600): Int? {
        if (principal >= threshold) return 0
        for (m in 1..maxMonths) {
            if (calculateFutureValueMonths(principal, monthlyPayment, annualPct, m) >= threshold) {
                return m
            }
        }
        return null
    }

    /**
     * Genera la etiqueta del mes objetivo (e.g. "Aug 2029")
     */
    fun calculateTargetDateLabel(monthsFromNow: Int, baseDate: LocalDate = LocalDate.now()): String {
        val targetDate = baseDate.plusMonths(monthsFromNow.toLong())
        val monthStr = monthNames[targetDate.monthValue - 1]
        return "$monthStr ${targetDate.year}"
    }

    /**
     * Genera la proyección patrimonial completa
     */
    fun generateEstimate(request: EstimateRequest, currentNetWorthUSD: Double): EstimateResponse {
        val principal = request.initialPrincipal ?: currentNetWorthUSD
        val yearlyList = mutableListOf<YearlyProjection>()

        for (year in 1..request.years) {
            val fv = calculateFutureValueYears(principal, request.contribution, request.yieldPct, year)
            val totalInvested = principal + request.contribution * year * 12
            val interestEarned = (fv - totalInvested).coerceAtLeast(0.0)
            yearlyList.add(
                YearlyProjection(
                    year = year,
                    totalInvested = (totalInvested * 100.0).roundToLong() / 100.0,
                    futureValue = (fv * 100.0).roundToLong() / 100.0,
                    interestEarned = (interestEarned * 100.0).roundToLong() / 100.0
                )
            )
        }

        val totalInvested = principal + request.contribution * request.years * 12
        val totalFV = calculateFutureValueYears(principal, request.contribution, request.yieldPct, request.years)
        val totalInterest = (totalFV - totalInvested).coerceAtLeast(0.0)

        // Hitos financieros predeterminados
        val defaultMilestones = listOf(100_000.0, 250_000.0, 500_000.0, 1_000_000.0, 2_000_000.0)
        val milestoneProjections = defaultMilestones.map { target ->
            val isAchieved = principal >= target
            val months = if (isAchieved) 0 else monthsToReach(target, principal, request.contribution, request.yieldPct)
            val dateLabel = months?.let { calculateTargetDateLabel(it) }
            MilestoneProjection(
                milestoneAmount = target,
                monthsRequired = months,
                targetDateLabel = dateLabel,
                isAchieved = isAchieved
            )
        }

        return EstimateResponse(
            initialPrincipal = (principal * 100.0).roundToLong() / 100.0,
            monthlyContribution = request.contribution,
            annualYieldPct = request.yieldPct,
            totalYears = request.years,
            totalInvested = (totalInvested * 100.0).roundToLong() / 100.0,
            totalFutureValue = (totalFV * 100.0).roundToLong() / 100.0,
            totalInterestEarned = (totalInterest * 100.0).roundToLong() / 100.0,
            yearlyBreakdown = yearlyList,
            milestones = milestoneProjections
        )
    }
}
