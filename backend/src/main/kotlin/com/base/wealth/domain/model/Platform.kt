package com.base.wealth.domain.model

import java.time.Instant

data class Platform(
    val userId: UserId,
    val name: PlatformName,
    val type: PlatformType,
    val createdAt: Instant,
)
