package com.base.wealth.domain.model

import java.time.Instant

data class Holding(
    val id: HoldingId,
    val userId: UserId,
    val name: String,
    val assetClass: AssetClass,
    val platform: PlatformName,
    val value: Money,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(name.isNotBlank()) { "Holding name must not be blank" }
        require(name.length <= MAX_NAME_LENGTH) { "Holding name must not exceed $MAX_NAME_LENGTH characters" }
    }

    fun patch(
        name: String?,
        assetClass: AssetClass?,
        platform: PlatformName?,
        value: Money?,
        now: Instant,
    ): Holding =
        copy(
            name = name?.trim() ?: this.name,
            assetClass = assetClass ?: this.assetClass,
            platform = platform ?: this.platform,
            value = value ?: this.value,
            updatedAt = now,
        )

    companion object {
        private const val MAX_NAME_LENGTH = 120

        fun create(
            userId: UserId,
            name: String,
            assetClass: AssetClass,
            platform: PlatformName,
            value: Money,
            now: Instant,
        ): Holding = Holding(HoldingId.new(), userId, name.trim(), assetClass, platform, value, now, now)
    }
}
