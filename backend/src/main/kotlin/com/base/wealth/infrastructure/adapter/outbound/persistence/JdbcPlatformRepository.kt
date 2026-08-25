package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.PlatformRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.util.UUID

@Repository
class JdbcPlatformRepository(
    private val jdbcClient: JdbcClient,
) : PlatformRepository {
    override fun findAll(userId: UserId): List<Platform> =
        jdbcClient
            .sql("SELECT * FROM platforms WHERE user_id = :userId ORDER BY name")
            .param("userId", userId.value)
            .query(::mapRow)
            .list()

    override fun findByName(
        userId: UserId,
        name: PlatformName,
    ): Platform? =
        jdbcClient
            .sql("SELECT * FROM platforms WHERE user_id = :userId AND lower(name) = lower(:name)")
            .param("userId", userId.value)
            .param("name", name.value)
            .query(::mapRow)
            .optional()
            .orElse(null)

    // The unique index is on lower(name), which ON CONFLICT can't target directly — resolve the
    // canonical name first, then insert only if truly missing (data-model.md §3).
    override fun ensureExists(
        userId: UserId,
        name: PlatformName,
        now: Instant,
    ): PlatformName {
        findByName(userId, name)?.let { return it.name }
        jdbcClient
            .sql("INSERT INTO platforms (user_id, name, type, created_at) VALUES (:userId, :name, :type, :now)")
            .param("userId", userId.value)
            .param("name", name.value)
            .param("type", PlatformType.OTHER.value)
            .param("now", now.toSqlTimestamp())
            .update()
        return name
    }

    override fun save(platform: Platform): Platform {
        jdbcClient
            .sql(
                """
                INSERT INTO platforms (user_id, name, type, created_at) VALUES (:userId, :name, :type, :now)
                ON CONFLICT (user_id, name) DO UPDATE SET type = EXCLUDED.type
                """.trimIndent(),
            ).param("userId", platform.userId.value)
            .param("name", platform.name.value)
            .param("type", platform.type.value)
            .param("now", platform.createdAt.toSqlTimestamp())
            .update()
        return platform
    }

    override fun update(
        userId: UserId,
        currentName: PlatformName,
        newName: PlatformName?,
        newType: PlatformType?,
    ): Platform? {
        if (newName == null && newType == null) return findByName(userId, currentName)
        val sql =
            buildString {
                append("UPDATE platforms SET ")
                val sets = mutableListOf<String>()
                if (newName != null) sets.add("name = :newName")
                if (newType != null) sets.add("type = :newType")
                append(sets.joinToString(", "))
                append(" WHERE user_id = :userId AND lower(name) = lower(:currentName)")
            }
        val spec =
            jdbcClient
                .sql(sql)
                .param("userId", userId.value)
                .param("currentName", currentName.value)
        if (newName != null) spec.param("newName", newName.value)
        if (newType != null) spec.param("newType", newType.value)
        val updated = spec.update()
        return if (updated == 0) null else findByName(userId, newName ?: currentName)
    }

    override fun deleteByName(
        userId: UserId,
        name: PlatformName,
    ): Boolean =
        jdbcClient
            .sql("DELETE FROM platforms WHERE user_id = :userId AND lower(name) = lower(:name)")
            .param("userId", userId.value)
            .param("name", name.value)
            .update() > 0

    override fun countHoldings(
        userId: UserId,
        name: PlatformName,
    ): Int =
        jdbcClient
            .sql("SELECT count(*) FROM holdings WHERE user_id = :userId AND platform_name = :name")
            .param("userId", userId.value)
            .param("name", name.value)
            .query(Int::class.java)
            .single()

    private fun mapRow(
        rs: ResultSet,
        @Suppress("UNUSED_PARAMETER") rowNum: Int,
    ): Platform =
        Platform(
            userId = UserId(rs.getObject("user_id", UUID::class.java)),
            name = PlatformName.of(rs.getString("name")),
            type = PlatformType.of(rs.getString("type")),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
}
