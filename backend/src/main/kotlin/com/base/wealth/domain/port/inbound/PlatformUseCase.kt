package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.PlatformMeta

interface PlatformUseCase {
    fun getAllPlatforms(): List<PlatformMeta>
}
