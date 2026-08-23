package com.base.wealth.infrastructure.adapter.outbound.persistence.kv

import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

// Backs onto the `kv_store` table (key TEXT primary key, value JSONB) in the Supabase
// Postgres database — see supabase/schema.sql. Active only under the "prod" profile.
@Repository
@Profile("prod")
class PostgresKvStore(private val jdbc: JdbcTemplate) : KvStore {

    override fun get(key: String): String? =
        jdbc.query("SELECT value FROM kv_store WHERE key = ?", { rs, _ -> rs.getString("value") }, key)
            .firstOrNull()

    override fun scan(prefix: String): Map<String, String> =
        jdbc.query(
            "SELECT key, value FROM kv_store WHERE key LIKE ?",
            { rs, _ -> rs.getString("key") to rs.getString("value") },
            "$prefix%"
        ).toMap()

    override fun put(key: String, value: String) {
        jdbc.update(
            """
            INSERT INTO kv_store (key, value, updated_at) VALUES (?, ?::jsonb, now())
            ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = now()
            """.trimIndent(),
            key, value
        )
    }

    override fun delete(key: String): Boolean =
        jdbc.update("DELETE FROM kv_store WHERE key = ?", key) > 0
}
