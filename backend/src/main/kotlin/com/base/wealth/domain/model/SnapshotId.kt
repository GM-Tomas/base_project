package com.base.wealth.domain.model

import java.util.UUID

@JvmInline
value class SnapshotId(
    val value: UUID,
) {
    companion object {
        fun new(): SnapshotId = SnapshotId(UUID.randomUUID())
    }
}
