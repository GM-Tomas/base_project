package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.EstimateRequest
import com.base.wealth.application.dto.EstimateResponse
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.domain.model.HistorySnapshot
import com.base.wealth.domain.port.inbound.WealthUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/wealth")
@Tag(name = "Wealth", description = "Endpoints de resumen patrimonial, proyecciones financieras e histórico")
class WealthController(
    private val wealthUseCase: WealthUseCase
) {

    @GetMapping("/summary")
    @Operation(summary = "Obtiene el resumen consolidado de patrimonio neto, métricas y distribuciones")
    fun getSummary(): ResponseEntity<WealthSummaryResponse> {
        return ResponseEntity.ok(wealthUseCase.getSummary())
    }

    @PostMapping("/estimate")
    @Operation(summary = "Calcula la proyección de interés compuesto y progreso de hitos financieros")
    fun calculateEstimate(@Valid @RequestBody request: EstimateRequest): ResponseEntity<EstimateResponse> {
        return ResponseEntity.ok(wealthUseCase.calculateEstimate(request))
    }

    @GetMapping("/history")
    @Operation(summary = "Obtiene la serie temporal histórica del portafolio")
    fun getHistory(): ResponseEntity<List<HistorySnapshot>> {
        return ResponseEntity.ok(wealthUseCase.getHistory())
    }
}
