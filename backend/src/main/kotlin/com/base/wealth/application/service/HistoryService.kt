package com.base.wealth.application.service

import com.base.wealth.domain.model.HistorySnapshot
import com.base.wealth.domain.port.inbound.HistoryUseCase
import org.springframework.stereotype.Service

@Service
class HistoryService : HistoryUseCase {

    private val initialHistory = listOf(
        HistorySnapshot("Jan", 61200.0),
        HistorySnapshot("Feb", 64800.0),
        HistorySnapshot("Mar", 68950.0),
        HistorySnapshot("Apr", 66300.0),
        HistorySnapshot("May", 73100.0),
        HistorySnapshot("Jun", 76800.0),
        HistorySnapshot("Jul", 79400.0),
        HistorySnapshot("Aug", 84250.0)
    )

    override fun getHistorySnapshots(): List<HistorySnapshot> = initialHistory
}
