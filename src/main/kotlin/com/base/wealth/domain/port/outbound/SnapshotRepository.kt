package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.UserId
import java.time.Instant

interface SnapshotRepository {
    fun findAll(
        userId: UserId,
        from: Instant? = null,
        to: Instant? = null,
    ): List<NetWorthSnapshot>

    fun save(snapshot: NetWorthSnapshot): NetWorthSnapshot

    fun existsAt(
        userId: UserId,
        capturedAt: Instant,
    ): Boolean

    /** First snapshot captured in [year] (server's UTC calendar) — the YTD baseline (CA-05.7). */
    fun findFirstOfYear(
        userId: UserId,
        year: Int,
    ): NetWorthSnapshot?

    /** Fallback YTD baseline when there's none yet this year, but there's history. */
    fun findEarliest(userId: UserId): NetWorthSnapshot?
}
