package com.base.wealth.domain.port.inbound

import com.base.wealth.application.dto.WealthSummaryResponse
import com.base.wealth.domain.model.UserId

interface WealthUseCase {
    fun getSummary(userId: UserId): WealthSummaryResponse
}
