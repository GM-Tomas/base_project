package com.base.wealth.domain.port.inbound

import com.base.wealth.application.dto.CreateHoldingRequest
import com.base.wealth.application.dto.UpdateHoldingRequest
import com.base.wealth.domain.model.Holding

interface HoldingUseCase {
    fun getAllHoldings(): List<Holding>
    fun getHoldingById(id: Long): Holding
    fun createHolding(request: CreateHoldingRequest): Holding
    fun updateHolding(id: Long, request: UpdateHoldingRequest): Holding
    fun deleteHolding(id: Long)
    fun getTotalNetWorthUSD(): Double
}
