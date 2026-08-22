package com.base.wealth.application.service

import com.base.wealth.domain.model.PlatformMeta
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.port.inbound.PlatformUseCase
import org.springframework.stereotype.Service

@Service
class PlatformService : PlatformUseCase {

    private val platforms = listOf(
        PlatformMeta("Balanz", PlatformType.BROKER),
        PlatformMeta("Mercado Pago", PlatformType.WALLET),
        PlatformMeta("Banco Galicia", PlatformType.BANK),
        PlatformMeta("Nexo", PlatformType.EXCHANGE),
        PlatformMeta("Binance", PlatformType.EXCHANGE)
    )

    override fun getAllPlatforms(): List<PlatformMeta> = platforms
}
