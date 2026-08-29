package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId

/**
 * Read model for the dashboard: SQL-side SUM/GROUP BY, not findAll()+groupBy in the JVM (NFR-2,
 * corrects fuga A5). Deliberately separate from [HoldingRepository]/[PlatformRepository] — those
 * are the write model.
 */
interface WealthAggregationPort {
    fun netWorth(userId: UserId): Money

    fun byAssetClass(userId: UserId): List<AssetClassAggregate>

    /** Includes platforms with no holdings (value = 0) — see data-model.md §3. */
    fun byPlatform(userId: UserId): List<PlatformAggregate>
}

data class AssetClassAggregate(
    val assetClass: AssetClass,
    val value: Money,
    val count: Int,
)

data class PlatformAggregate(
    val name: PlatformName,
    val type: PlatformType,
    val value: Money,
    val count: Int,
)
