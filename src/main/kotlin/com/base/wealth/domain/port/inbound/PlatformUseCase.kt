package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.UserId

interface PlatformUseCase {
    fun getAllPlatforms(userId: UserId): List<Platform>

    fun createPlatform(command: CreatePlatformCommand): Platform

    fun patchPlatform(command: PatchPlatformCommand): Platform

    fun deletePlatform(
        userId: UserId,
        name: String,
    )
}
