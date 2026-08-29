package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId

interface HoldingUseCase {
    fun getAllHoldings(
        userId: UserId,
        assetClass: AssetClass? = null,
        platform: PlatformName? = null,
    ): List<Holding>

    fun getHoldingById(
        userId: UserId,
        id: HoldingId,
    ): Holding

    fun createHolding(command: CreateHoldingCommand): Holding

    fun updateHolding(command: PatchHoldingCommand): Holding

    fun deleteHolding(
        userId: UserId,
        id: HoldingId,
    )
}
