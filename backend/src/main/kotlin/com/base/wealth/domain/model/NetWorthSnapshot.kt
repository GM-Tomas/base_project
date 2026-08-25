package com.base.wealth.domain.model

import java.time.Instant

data class NetWorthSnapshot(
    val id: SnapshotId,
    val userId: UserId,
    val capturedAt: Instant,
    val totalValue: Money,
)
