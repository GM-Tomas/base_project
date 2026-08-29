package com.base.wealth.domain.model

import java.util.UUID

@JvmInline
value class HoldingId(
    val value: UUID,
) {
    companion object {
        fun new(): HoldingId = HoldingId(UUID.randomUUID())
    }
}
