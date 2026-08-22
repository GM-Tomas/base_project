package com.base.wealth.domain.port.inbound

import com.base.wealth.application.dto.EstimateRequest
import com.base.wealth.application.dto.EstimateResponse
import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.domain.model.HistorySnapshot

interface WealthUseCase {
    fun getSummary(): WealthSummaryResponse
    fun calculateEstimate(request: EstimateRequest): EstimateResponse
    fun getHistory(): List<HistorySnapshot>
}
