package com.base.wealth.domain.model

import java.time.Instant
import java.util.UUID

data class Task(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Instant = Instant.now(),
)
