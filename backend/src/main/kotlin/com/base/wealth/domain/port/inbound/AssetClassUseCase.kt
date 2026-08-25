package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.UserId

interface AssetClassUseCase {
    /** Server defaults ∪ classes the user actually has holdings in (F9). */
    fun getAvailableAssetClasses(userId: UserId): AvailableAssetClasses
}

data class AvailableAssetClasses(
    val defaults: List<String>,
    val inUse: List<String>,
    val all: List<String>,
)
