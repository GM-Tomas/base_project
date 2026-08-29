package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.UserId
import java.math.BigDecimal
import java.time.Instant

data class SnapshotWithChange(
    val snapshot: NetWorthSnapshot,
    val changePctFromPrevious: BigDecimal?,
)

interface SnapshotUseCase {
    /** Computes and persists the user's current net worth (CA-06.1, CA-06.2 — never client-supplied). */
    fun createSnapshot(userId: UserId): NetWorthSnapshot

    /** Ordered by `capturedAt` ascending, each point paired with its change from the previous one. */
    fun getSnapshots(
        userId: UserId,
        from: Instant? = null,
        to: Instant? = null,
    ): List<SnapshotWithChange>
}
