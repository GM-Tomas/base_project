package com.base.wealth.application.service

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreatePlatformCommand
import com.base.wealth.domain.port.inbound.PatchPlatformCommand
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.PlatformRepository
import com.base.wealth.exception.DuplicateResourceException
import com.base.wealth.exception.ResourceInUseException
import com.base.wealth.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class PlatformServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val platformRepository = mockk<PlatformRepository>()
    private val clock = mockk<ClockPort>()
    private val service = PlatformService(platformRepository, clock)

    @Test
    @DisplayName("createPlatform rejects a duplicate name, case-insensitively (CA-04.2)")
    fun createPlatformRejectsDuplicate() {
        every { platformRepository.findByName(userId, PlatformName.of("binance")) } returns
            Platform(userId, PlatformName.of("Binance"), PlatformType.of("Exchange"), Instant.now())

        assertThrows(DuplicateResourceException::class.java) {
            service.createPlatform(CreatePlatformCommand(userId, "binance", "Exchange"))
        }
    }

    @Test
    @DisplayName("patchPlatform renaming to your own current name (case-only change) doesn't conflict with itself")
    fun patchPlatformAllowsRenamingToSelf() {
        val updated = Platform(userId, PlatformName.of("BINANCE"), PlatformType.of("Exchange"), Instant.now())
        every {
            platformRepository.update(userId, PlatformName.of("Binance"), PlatformName.of("BINANCE"), null)
        } returns updated

        val result = service.patchPlatform(PatchPlatformCommand(userId, "Binance", newName = "BINANCE"))

        assertEquals(PlatformName.of("BINANCE"), result.name)
    }

    @Test
    @DisplayName("patchPlatform throws when the platform doesn't exist for this user")
    fun patchPlatformNotFound() {
        every { platformRepository.update(userId, PlatformName.of("Ghost"), null, null) } returns null

        assertThrows(ResourceNotFoundException::class.java) {
            service.patchPlatform(PatchPlatformCommand(userId, "Ghost"))
        }
    }

    @Test
    @DisplayName("deletePlatform with holdings attached responds with the exact count (CA-04.3)")
    fun deletePlatformInUse() {
        every { platformRepository.countHoldings(userId, PlatformName.of("Balanz")) } returns 3

        val ex =
            assertThrows(ResourceInUseException::class.java) {
                service.deletePlatform(userId, "Balanz")
            }
        assertEquals(true, ex.message?.contains("3"))
    }

    @Test
    @DisplayName("deletePlatform with no holdings deletes it")
    fun deletePlatformEmpty() {
        every { platformRepository.countHoldings(userId, PlatformName.of("Nexo")) } returns 0
        every { platformRepository.deleteByName(userId, PlatformName.of("Nexo")) } returns true

        service.deletePlatform(userId, "Nexo")
    }
}
