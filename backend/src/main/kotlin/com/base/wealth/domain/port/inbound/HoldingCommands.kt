package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.UserId

// Plain values, not domain value objects (AssetClass/Money/...): normalization/validation is the
// service's job when it turns these into a Holding — a command is a wire-shaped request, not yet
// a validated domain object.
data class CreateHoldingCommand(
    val userId: UserId,
    val name: String,
    val assetClass: String,
    val platform: String,
    val valueUsd: Double,
)

data class PatchHoldingCommand(
    val userId: UserId,
    val id: HoldingId,
    val name: String? = null,
    val assetClass: String? = null,
    val platform: String? = null,
    val valueUsd: Double? = null,
)
