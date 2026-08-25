package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.support.PostgresTestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.simple.JdbcClient
import java.time.Instant
import java.util.UUID

// Requires Docker (Testcontainers) — see support/PostgresTestBase.
@DisplayName("JdbcHoldingRepository (real Postgres)")
class JdbcHoldingRepositoryTest : PostgresTestBase() {
    @Autowired
    private lateinit var repository: JdbcHoldingRepository

    @Autowired
    private lateinit var jdbcClient: JdbcClient

    private fun seedPlatform(
        userId: UserId,
        name: PlatformName,
    ) {
        // platforms.user_id FKs to auth.users(id) — real in Supabase, stood in for tests by
        // testcontainers-init.sql, but that stand-in starts empty; seed the row this test's
        // random UserId needs or the FK insert below fails.
        jdbcClient
            .sql("INSERT INTO auth.users (id) VALUES (:u) ON CONFLICT (id) DO NOTHING")
            .param("u", userId.value)
            .update()
        jdbcClient
            .sql("INSERT INTO platforms (user_id, name, type, created_at) VALUES (:u, :n, 'Other', now())")
            .param("u", userId.value)
            .param("n", name.value)
            .update()
    }

    private fun newHolding(
        userId: UserId,
        platform: PlatformName,
        value: Double = 100.0,
    ) = Holding.create(userId, "AAPL", AssetClass.of("Equity"), platform, Money.of(value), Instant.now())

    @Test
    @DisplayName("save + findById round-trips correctly")
    fun saveAndFindById() {
        val userId = UserId(UUID.randomUUID())
        val platform = PlatformName.of("Balanz")
        seedPlatform(userId, platform)
        val holding = newHolding(userId, platform)

        repository.save(holding)
        val found = repository.findById(userId, holding.id)

        assertEquals(holding.copy(createdAt = found!!.createdAt, updatedAt = found.updatedAt), found)
    }

    @Test
    @DisplayName("a holding is invisible to any other user (NFR-3 isolation)")
    fun isolatedBetweenUsers() {
        val ownerId = UserId(UUID.randomUUID())
        val strangerId = UserId(UUID.randomUUID())
        val platform = PlatformName.of("Balanz")
        seedPlatform(ownerId, platform)
        val holding = newHolding(ownerId, platform)
        repository.save(holding)

        assertNull(repository.findById(strangerId, holding.id))
        assertTrue(repository.findAll(strangerId).isEmpty())
    }

    @Test
    @DisplayName("deleteById only deletes when the id belongs to that user")
    fun deleteRespectsOwnership() {
        val ownerId = UserId(UUID.randomUUID())
        val strangerId = UserId(UUID.randomUUID())
        val platform = PlatformName.of("Balanz")
        seedPlatform(ownerId, platform)
        val holding = newHolding(ownerId, platform)
        repository.save(holding)

        assertTrue(!repository.deleteById(strangerId, holding.id))
        assertTrue(repository.deleteById(ownerId, holding.id))
    }
}
