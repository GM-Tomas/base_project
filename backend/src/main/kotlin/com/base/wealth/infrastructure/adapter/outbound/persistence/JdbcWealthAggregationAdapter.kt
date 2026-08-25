package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.AssetClassAggregate
import com.base.wealth.domain.port.outbound.PlatformAggregate
import com.base.wealth.domain.port.outbound.WealthAggregationPort
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal

/**
 * SQL-side SUM/GROUP BY (NFR-2) — see data-model.md §3 for the exact queries and why byPlatform
 * LEFT JOINs from platforms.
 */
@Repository
class JdbcWealthAggregationAdapter(
    private val jdbcClient: JdbcClient,
) : WealthAggregationPort {
    override fun netWorth(userId: UserId): Money {
        val total =
            jdbcClient
                .sql("SELECT COALESCE(SUM(value_usd), 0) FROM holdings WHERE user_id = :userId")
                .param("userId", userId.value)
                .query(BigDecimal::class.java)
                .single()
        return Money.of(total)
    }

    override fun byAssetClass(userId: UserId): List<AssetClassAggregate> =
        jdbcClient
            .sql(
                "SELECT asset_class, SUM(value_usd) AS total, COUNT(*) AS cnt FROM holdings " +
                    "WHERE user_id = :userId GROUP BY asset_class ORDER BY total DESC",
            ).param("userId", userId.value)
            .query { rs, _ ->
                AssetClassAggregate(
                    assetClass = AssetClass.of(rs.getString("asset_class")),
                    value = Money.of(rs.getBigDecimal("total")),
                    count = rs.getInt("cnt"),
                )
            }.list()

    override fun byPlatform(userId: UserId): List<PlatformAggregate> =
        jdbcClient
            .sql(
                """
                SELECT p.name, p.type, COALESCE(SUM(h.value_usd), 0) AS total, COUNT(h.id) AS cnt
                FROM platforms p
                LEFT JOIN holdings h ON h.user_id = p.user_id AND h.platform_name = p.name
                WHERE p.user_id = :userId
                GROUP BY p.name, p.type
                ORDER BY total DESC, p.name
                """.trimIndent(),
            ).param("userId", userId.value)
            .query { rs, _ ->
                PlatformAggregate(
                    name = PlatformName.of(rs.getString("name")),
                    type = PlatformType.of(rs.getString("type")),
                    value = Money.of(rs.getBigDecimal("total")),
                    count = rs.getInt("cnt"),
                )
            }.list()
}
