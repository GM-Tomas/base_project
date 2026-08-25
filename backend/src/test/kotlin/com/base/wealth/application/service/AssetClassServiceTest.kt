package com.base.wealth.application.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.HoldingRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class AssetClassServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val holdingRepository = mockk<HoldingRepository>()
    private val defaults = listOf("Cash", "Equity")
    private val service = AssetClassService(holdingRepository, defaults)

    @Test
    @DisplayName("all is defaults union in-use, without duplicates (F9)")
    fun combinesDefaultsAndInUse() {
        every { holdingRepository.assetClassesInUse(userId) } returns
            listOf(AssetClass.of("Cash"), AssetClass.of("Real Estate"))

        val result = service.getAvailableAssetClasses(userId)

        assertEquals(listOf("Cash", "Equity"), result.defaults)
        assertEquals(listOf("Cash", "Real Estate"), result.inUse)
        assertEquals(listOf("Cash", "Equity", "Real Estate"), result.all)
    }

    @Test
    @DisplayName("with no holdings, all is exactly the defaults")
    fun noHoldingsMeansOnlyDefaults() {
        every { holdingRepository.assetClassesInUse(userId) } returns emptyList()

        val result = service.getAvailableAssetClasses(userId)

        assertEquals(defaults, result.all)
    }
}
