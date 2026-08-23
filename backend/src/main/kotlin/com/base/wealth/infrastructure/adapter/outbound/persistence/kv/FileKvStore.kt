package com.base.wealth.infrastructure.adapter.outbound.persistence.kv

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import java.io.File

// ponytail: local/test KVS is one JSON(B) file on disk instead of a real Postgres —
// swap for PostgresKvStore (profile "prod") once deployed against Supabase.
@Repository
@Profile("!prod")
class FileKvStore(
    @Value("\${wealth.kv.file:./data/kvstore.json}") path: String
) : KvStore {

    private val mapper = jacksonObjectMapper()
    private val file = File(path)
    private val lock = Any()
    private val store: LinkedHashMap<String, JsonNode> = load()

    private fun load(): LinkedHashMap<String, JsonNode> {
        file.parentFile?.mkdirs()
        if (!file.exists()) return LinkedHashMap()
        return mapper.readValue(file)
    }

    private fun flush() = mapper.writerWithDefaultPrettyPrinter().writeValue(file, store)

    override fun get(key: String): String? = synchronized(lock) { store[key]?.toString() }

    override fun scan(prefix: String): Map<String, String> = synchronized(lock) {
        store.filterKeys { it.startsWith(prefix) }.mapValues { it.value.toString() }
    }

    override fun put(key: String, value: String) = synchronized(lock) {
        store[key] = mapper.readTree(value)
        flush()
    }

    override fun delete(key: String): Boolean = synchronized(lock) {
        val existed = store.remove(key) != null
        if (existed) flush()
        existed
    }
}
