package com.base.wealth.application.service

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreateHoldingCommand
import com.base.wealth.domain.port.inbound.PatchHoldingCommand
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.HoldingRepository
import com.base.wealth.domain.port.outbound.PlatformRepository
import com.base.wealth.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class HoldingServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val holdingRepository = mockk<HoldingRepository>()
    private val platformRepository = mockk<PlatformRepository>()
    private val clock = mockk<ClockPort>()
    private val service = HoldingService(holdingRepository, platformRepository, clock)

    @Test
    @DisplayName("getHoldingById throws when the holding doesn't exist for this user (CA-01.4: 404, not 403)")
    fun getHoldingByIdNotFound() {
        val id = HoldingId.new()
        every { holdingRepository.findById(userId, id) } returns null

        assertThrows(ResourceNotFoundException::class.java) { service.getHoldingById(userId, id) }
    }

    @Test
    @DisplayName("createHolding creates the platform implicitly, in the same call (CA-02.2)")
    fun createHoldingEnsuresPlatformExists() {
        val now = Instant.parse("2026-06-15T10:00:00Z")
        every { clock.now() } returns now
        every { platformRepository.ensureExists(userId, PlatformName.of("Binance"), now) } returns
            PlatformName.of("Binance")
        val saved = slot<Holding>()
        every { holdingRepository.save(capture(saved)) } answers { saved.captured }

        val result =
            service.createHolding(CreateHoldingCommand(userId, "Solana", "Crypto", "Binance", 2250.0))

        assertEquals("Solana", result.name)
        assertEquals(Money.of(2250.0), result.value)
        verify { platformRepository.ensureExists(userId, PlatformName.of("Binance"), now) }
    }

    @Test
    @DisplayName("updateHolding applies only the fields present in the command")
    fun updateHoldingPatchesPartially() {
        val existing =
            Holding.create(
                userId,
                "AAPL",
                AssetClass.of("Equity"),
                PlatformName.of("Balanz"),
                Money.of(100.0),
                Instant.parse("2026-01-01T00:00:00Z"),
            )
        val now = Instant.parse("2026-06-15T10:00:00Z")
        every { holdingRepository.findById(userId, existing.id) } returns existing
        every { clock.now() } returns now
        val saved = slot<Holding>()
        every { holdingRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.updateHolding(PatchHoldingCommand(userId, existing.id, valueUsd = 150.0))

        assertEquals("AAPL", result.name)
        assertEquals(Money.of(150.0), result.value)
        assertEquals(now, result.updatedAt)
    }

    @Test
    @DisplayName("deleteHolding throws when nothing was deleted")
    fun deleteHoldingNotFound() {
        val id = HoldingId.new()
        every { holdingRepository.deleteById(userId, id) } returns false

        assertThrows(ResourceNotFoundException::class.java) { service.deleteHolding(userId, id) }
    }
}
