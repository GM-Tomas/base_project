package com.base.wealth.application.service

import com.base.wealth.domain.model.PlatformMeta
import com.base.wealth.domain.port.inbound.PlatformUseCase
import org.springframework.stereotype.Service

@Service
class PlatformService : PlatformUseCase {

    private val platforms = listOf(
        PlatformMeta("Balanz", "Broker"),
        PlatformMeta("Mercado Pago", "Wallet"),
        PlatformMeta("Banco Galicia", "Bank"),
        PlatformMeta("Nexo", "Exchange"),
        PlatformMeta("Binance", "Exchange")
    )

    override fun getAllPlatforms(): List<PlatformMeta> = platforms
}
