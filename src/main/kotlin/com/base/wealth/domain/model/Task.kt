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

data class Profile(
    val id: UUID,
    val username: String,
    val fullName: String?,
    val avatarUrl: String?,
    val updatedAt: Instant? = null,
)
