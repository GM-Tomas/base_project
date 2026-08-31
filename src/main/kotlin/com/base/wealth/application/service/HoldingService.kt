package com.base.wealth.application.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreateHoldingCommand
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.domain.port.inbound.PatchHoldingCommand
import com.base.wealth.domain.port.outbound.HoldingRepository
import com.base.wealth.domain.port.outbound.PlatformRepository
import com.base.wealth.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class HoldingService(
    private val holdingRepository: HoldingRepository,
    private val platformRepository: PlatformRepository,
    private val clock: Clock,
) : HoldingUseCase {
    override fun getAllHoldings(
        userId: UserId,
        assetClass: AssetClass?,
        platform: PlatformName?,
    ): List<Holding> = holdingRepository.findAll(userId, assetClass, platform)

    override fun getHoldingById(
        userId: UserId,
        id: HoldingId,
    ): Holding =
        holdingRepository.findById(userId, id)
            ?: throw ResourceNotFoundException("No se encontró el holding con ID: ${id.value}")

    @Transactional
    override fun createHolding(command: CreateHoldingCommand): Holding {
        val now = clock.instant()
        val platform = platformRepository.ensureExists(command.userId, PlatformName.of(command.platform), now)
        val holding =
            Holding.create(
                userId = command.userId,
                name = command.name,
                assetClass = AssetClass.of(command.assetClass),
                platform = platform,
                value = Money.of(command.valueUsd),
                now = now,
            )
        return holdingRepository.save(holding)
    }

    override fun updateHolding(command: PatchHoldingCommand): Holding {
        val existing = getHoldingById(command.userId, command.id)
        val updated =
            existing.patch(
                name = command.name,
                assetClass = command.assetClass?.let { AssetClass.of(it) },
                platform = command.platform?.let { PlatformName.of(it) },
                value = command.valueUsd?.let { Money.of(it) },
                now = clock.instant(),
            )
        return holdingRepository.save(updated)
    }

    override fun deleteHolding(
        userId: UserId,
        id: HoldingId,
    ) {
        if (!holdingRepository.deleteById(userId, id)) {
            throw ResourceNotFoundException("No se encontró el holding con ID: ${id.value} para eliminar")
        }
    }
}
