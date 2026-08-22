package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.HistorySnapshot

interface HistoryUseCase {
    fun getHistorySnapshots(): List<HistorySnapshot>
}
