package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.HoldingRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class JdbcHoldingRepository(
    private val jdbcClient: JdbcClient,
) : HoldingRepository {
    override fun findAll(
        userId: UserId,
        assetClass: AssetClass?,
        platform: PlatformName?,
    ): List<Holding> {
        val sql =
            buildString {
                append("SELECT * FROM holdings WHERE user_id = :userId")
                if (assetClass != null) append(" AND asset_class = :assetClass")
                if (platform != null) append(" AND platform_name = :platform")
                append(" ORDER BY created_at ASC")
            }
        val spec = jdbcClient.sql(sql).param("userId", userId.value)
        if (assetClass != null) spec.param("assetClass", assetClass.value)
        if (platform != null) spec.param("platform", platform.value)
        return spec.query(::mapRow).list()
    }

    override fun findById(
        userId: UserId,
        id: HoldingId,
    ): Holding? =
        jdbcClient
            .sql("SELECT * FROM holdings WHERE id = :id AND user_id = :userId")
            .param("id", id.value)
            .param("userId", userId.value)
            .query(::mapRow)
            .optional()
            .orElse(null)

    override fun save(holding: Holding): Holding {
        jdbcClient
            .sql(
                """
                INSERT INTO holdings (id, user_id, name, asset_class, platform_name, value_usd, created_at, updated_at)
                VALUES (:id, :userId, :name, :assetClass, :platform, :value, :createdAt, :updatedAt)
                ON CONFLICT (id) DO UPDATE SET
                    name = EXCLUDED.name,
                    asset_class = EXCLUDED.asset_class,
                    platform_name = EXCLUDED.platform_name,
                    value_usd = EXCLUDED.value_usd,
                    updated_at = EXCLUDED.updated_at
                """.trimIndent(),
            ).param("id", holding.id.value)
            .param("userId", holding.userId.value)
            .param("name", holding.name)
            .param("assetClass", holding.assetClass.value)
            .param("platform", holding.platform.value)
            .param("value", holding.value.amount)
            .param("createdAt", holding.createdAt.toSqlTimestamp())
            .param("updatedAt", holding.updatedAt.toSqlTimestamp())
            .update()
        return holding
    }

    override fun deleteById(
        userId: UserId,
        id: HoldingId,
    ): Boolean =
        jdbcClient
            .sql("DELETE FROM holdings WHERE id = :id AND user_id = :userId")
            .param("id", id.value)
            .param("userId", userId.value)
            .update() > 0

    override fun assetClassesInUse(userId: UserId): List<AssetClass> =
        jdbcClient
            .sql("SELECT DISTINCT asset_class FROM holdings WHERE user_id = :userId ORDER BY asset_class")
            .param("userId", userId.value)
            .query { rs, _ -> AssetClass.of(rs.getString("asset_class")) }
            .list()

    private fun mapRow(
        rs: ResultSet,
        @Suppress("UNUSED_PARAMETER") rowNum: Int,
    ): Holding =
        Holding(
            id = HoldingId(rs.getObject("id", UUID::class.java)),
            userId = UserId(rs.getObject("user_id", UUID::class.java)),
            name = rs.getString("name"),
            assetClass = AssetClass.of(rs.getString("asset_class")),
            platform = PlatformName.of(rs.getString("platform_name")),
            value = Money.of(rs.getBigDecimal("value_usd")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at").toInstant(),
        )
}
