package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId

interface HoldingRepository {
    fun findAll(
        userId: UserId,
        assetClass: AssetClass? = null,
        platform: PlatformName? = null,
    ): List<Holding>

    fun findById(
        userId: UserId,
        id: HoldingId,
    ): Holding?

    fun save(holding: Holding): Holding

    fun deleteById(
        userId: UserId,
        id: HoldingId,
    ): Boolean

    /** Distinct classes currently in use, for F9 (`GET /asset-classes`). */
    fun assetClassesInUse(userId: UserId): List<AssetClass>
}
