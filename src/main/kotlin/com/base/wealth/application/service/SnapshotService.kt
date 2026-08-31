package com.base.wealth.application.service

import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.SnapshotId
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.SnapshotUseCase
import com.base.wealth.domain.port.inbound.SnapshotWithChange
import com.base.wealth.domain.port.outbound.SnapshotRepository
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import com.base.wealth.exception.DuplicateResourceException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SnapshotService(
    private val snapshotRepository: SnapshotRepository,
    private val wealthAggregationPort: WealthAggregationPort,
    private val clock: Clock,
) : SnapshotUseCase {
    // ponytail: check-then-act (existsAt then save) has a race window under true concurrency;
    // the DB's `snapshots_user_instant_uk` constraint (data-model.md §2) is the real guard against
    // corrupt data, this pre-check just turns the common case (double-click) into a clean 409
    // instead of a raw constraint-violation 500. Add a catch around save() if concurrent posts
    // from the same user in the same second become a real scenario.
    override fun createSnapshot(userId: UserId): NetWorthSnapshot {
        val capturedAt = clock.instant().truncatedTo(ChronoUnit.SECONDS)
        if (snapshotRepository.existsAt(userId, capturedAt)) {
            throw DuplicateResourceException("A snapshot already exists for $capturedAt")
        }
        val netWorth = wealthAggregationPort.netWorth(userId)
        return snapshotRepository.save(NetWorthSnapshot(SnapshotId.new(), userId, capturedAt, netWorth))
    }

    override fun getSnapshots(
        userId: UserId,
        from: Instant?,
        to: Instant?,
    ): List<SnapshotWithChange> {
        val snapshots = snapshotRepository.findAll(userId, from, to)
        return snapshots.mapIndexed { index, snapshot ->
            val previous = snapshots.getOrNull(index - 1)
            SnapshotWithChange(snapshot, previous?.let { snapshot.totalValue.growthPctFrom(it.totalValue) })
        }
    }
}
