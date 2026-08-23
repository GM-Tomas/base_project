package com.base.wealth.infrastructure.adapter.outbound.persistence.kv

/**
 * Generic key-value store. `value` is always a JSON string (stored as JSONB in Postgres,
 * or as a JSON blob per key in the local file store).
 */
interface KvStore {
    fun get(key: String): String?
    fun scan(prefix: String): Map<String, String>
    fun put(key: String, value: String)
    fun delete(key: String): Boolean
}
