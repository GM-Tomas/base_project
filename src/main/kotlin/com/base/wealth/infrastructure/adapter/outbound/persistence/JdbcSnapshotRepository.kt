package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.NetWorthSnapshot
import com.base.wealth.domain.model.SnapshotId
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.SnapshotRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@Repository
class JdbcSnapshotRepository(
    private val jdbcClient: JdbcClient,
) : SnapshotRepository {
    override fun findAll(
        userId: UserId,
        from: Instant?,
        to: Instant?,
    ): List<NetWorthSnapshot> {
        val sql =
            buildString {
                append("SELECT * FROM net_worth_snapshots WHERE user_id = :userId")
                if (from != null) append(" AND captured_at >= :from")
                if (to != null) append(" AND captured_at <= :to")
                append(" ORDER BY captured_at ASC")
            }
        val spec = jdbcClient.sql(sql).param("userId", userId.value)
        if (from != null) spec.param("from", from.toSqlTimestamp())
        if (to != null) spec.param("to", to.toSqlTimestamp())
        return spec.query(::mapRow).list()
    }

    override fun save(snapshot: NetWorthSnapshot): NetWorthSnapshot {
        jdbcClient
            .sql(
                "INSERT INTO net_worth_snapshots (id, user_id, captured_at, total_value_usd) " +
                    "VALUES (:id, :userId, :capturedAt, :value)",
            ).param("id", snapshot.id.value)
            .param("userId", snapshot.userId.value)
            .param("capturedAt", snapshot.capturedAt.toSqlTimestamp())
            .param("value", snapshot.totalValue.amount)
            .update()
        return snapshot
    }

    override fun existsAt(
        userId: UserId,
        capturedAt: Instant,
    ): Boolean =
        jdbcClient
            .sql("SELECT count(*) FROM net_worth_snapshots WHERE user_id = :userId AND captured_at = :capturedAt")
            .param("userId", userId.value)
            .param("capturedAt", capturedAt.toSqlTimestamp())
            .query(Int::class.java)
            .single() > 0

    override fun findFirstOfYear(
        userId: UserId,
        year: Int,
    ): NetWorthSnapshot? {
        val yearStart = Instant.parse("$year-01-01T00:00:00Z")
        return jdbcClient
            .sql(
                "SELECT * FROM net_worth_snapshots WHERE user_id = :userId AND captured_at >= :yearStart " +
                    "ORDER BY captured_at ASC LIMIT 1",
            ).param("userId", userId.value)
            .param("yearStart", yearStart.toSqlTimestamp())
            .query(::mapRow)
            .optional()
            .orElse(null)
    }

    override fun findEarliest(userId: UserId): NetWorthSnapshot? =
        jdbcClient
            .sql("SELECT * FROM net_worth_snapshots WHERE user_id = :userId ORDER BY captured_at ASC LIMIT 1")
            .param("userId", userId.value)
            .query(::mapRow)
            .optional()
            .orElse(null)

    private fun mapRow(
        rs: ResultSet,
        @Suppress("UNUSED_PARAMETER") rowNum: Int,
    ): NetWorthSnapshot =
        NetWorthSnapshot(
            id = SnapshotId(rs.getObject("id", UUID::class.java)),
            userId = UserId(rs.getObject("user_id", UUID::class.java)),
            capturedAt = rs.getTimestamp("captured_at").toInstant(),
            totalValue = Money.of(rs.getBigDecimal("total_value_usd")),
        )
}
