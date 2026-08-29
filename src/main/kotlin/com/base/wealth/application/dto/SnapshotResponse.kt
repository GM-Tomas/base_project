package com.base.wealth.application.dto

import java.time.Instant

data class SnapshotResponse(
    val id: String,
    val capturedAt: Instant,
    val totalValueUsd: Double,
    val changePctFromPrevious: Double? = null,
)
