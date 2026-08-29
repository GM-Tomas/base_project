package com.base.wealth.application.service

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.SnapshotId
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.SnapshotRepository
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import com.base.wealth.exception.DuplicateResourceException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class SnapshotServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val snapshotRepository = mockk<SnapshotRepository>()
    private val wealthAggregationPort = mockk<WealthAggregationPort>()
    private val clock = mockk<ClockPort>()
    private val service = SnapshotService(snapshotRepository, wealthAggregationPort, clock)

    @Test
    @DisplayName("computes the amount server-side and truncates capturedAt to the second (CA-06.1, CA-06.2)")
    fun createsSnapshotWithServerComputedAmount() {
        val now = Instant.parse("2026-06-15T10:30:00.123456Z")
        every { clock.now() } returns now
        every { snapshotRepository.existsAt(userId, any()) } returns false
        every { wealthAggregationPort.netWorth(userId) } returns Money.of(5000.0)
        val saved = slot<NetWorthSnapshot>()
        every { snapshotRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.createSnapshot(userId)

        assertEquals(Money.of(5000.0), result.totalValue)
        assertEquals(Instant.parse("2026-06-15T10:30:00Z"), result.capturedAt)
    }

    @Test
    @DisplayName("a second snapshot in the same second is rejected as a conflict (CA-06.4)")
    fun rejectsDuplicateInstant() {
        every { clock.now() } returns Instant.parse("2026-06-15T10:30:00Z")
        every { snapshotRepository.existsAt(userId, any()) } returns true

        assertThrows(DuplicateResourceException::class.java) { service.createSnapshot(userId) }
    }

    @Test
    @DisplayName("getSnapshots pairs each point with its change from the previous one, null on the first")
    fun computesChangeFromPrevious() {
        val first = NetWorthSnapshot(SnapshotId.new(), userId, Instant.parse("2026-01-01T00:00:00Z"), Money.of(1000.0))
        val second = NetWorthSnapshot(SnapshotId.new(), userId, Instant.parse("2026-02-01T00:00:00Z"), Money.of(1100.0))
        every { snapshotRepository.findAll(userId, null, null) } returns listOf(first, second)

        val result = service.getSnapshots(userId)

        assertNull(result[0].changePctFromPrevious)
        assertEquals(BigDecimal("10.0"), result[1].changePctFromPrevious)
    }
}
