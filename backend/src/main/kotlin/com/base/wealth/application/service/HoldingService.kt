package com.base.wealth.application.service

import com.base.wealth.application.dto.CreateHoldingRequest
import com.base.wealth.application.dto.UpdateHoldingRequest
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.domain.port.outbound.HoldingRepository
import com.base.wealth.exception.ResourceNotFoundException
import org.springframework.stereotype.Service

@Service
class HoldingService(
    private val holdingRepository: HoldingRepository
) : HoldingUseCase {

    override fun getAllHoldings(): List<Holding> = holdingRepository.findAll()

    override fun getHoldingById(id: Long): Holding =
        holdingRepository.findById(id) ?: throw ResourceNotFoundException("No se encontró el holding con ID: $id")

    override fun createHolding(request: CreateHoldingRequest): Holding {
        val newHolding = Holding(
            id = holdingRepository.nextId(),
            name = request.name,
            cls = request.cls,
            platform = request.platform,
            value = request.value
        )
        return holdingRepository.save(newHolding)
    }

    override fun updateHolding(id: Long, request: UpdateHoldingRequest): Holding {
        val existing = getHoldingById(id)
        val updated = existing.copy(
            name = request.name ?: existing.name,
            cls = request.cls ?: existing.cls,
            platform = request.platform ?: existing.platform,
            value = request.value ?: existing.value
        )
        return holdingRepository.save(updated)
    }

    override fun deleteHolding(id: Long) {
        if (!holdingRepository.deleteById(id)) {
            throw ResourceNotFoundException("No se encontró el holding con ID: $id para eliminar")
        }
    }

    override fun getTotalNetWorthUSD(): Double = holdingRepository.findAll().sumOf { it.value }
}
