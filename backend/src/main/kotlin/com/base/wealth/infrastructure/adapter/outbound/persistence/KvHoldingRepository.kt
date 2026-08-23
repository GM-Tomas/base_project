package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.port.outbound.HoldingRepository
import com.base.wealth.infrastructure.adapter.outbound.persistence.kv.KvStore
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicLong

private const val PREFIX = "holding:"

@Repository
class KvHoldingRepository(private val kv: KvStore) : HoldingRepository {

    private val mapper = jacksonObjectMapper()
    private val idCounter: AtomicLong

    init {
        // ponytail: two scans, startup-only — not worth threading a shared local through seed()
        if (kv.scan(PREFIX).isEmpty()) seed()
        val maxId = kv.scan(PREFIX).keys.mapNotNull { it.removePrefix(PREFIX).toLongOrNull() }.maxOrNull() ?: 10L
        idCounter = AtomicLong(maxId)
    }

    override fun findAll(): List<Holding> =
        kv.scan(PREFIX).values.map { mapper.readValue<Holding>(it) }.sortedBy { it.id }

    override fun findById(id: Long): Holding? = kv.get(PREFIX + id)?.let { mapper.readValue<Holding>(it) }

    override fun nextId(): Long = idCounter.incrementAndGet()

    override fun save(holding: Holding): Holding {
        kv.put(PREFIX + holding.id, mapper.writeValueAsString(holding))
        return holding
    }

    override fun deleteById(id: Long): Boolean = kv.delete(PREFIX + id)

    // Datos semilla alineados con el frontend BASE Wealth
    private fun seed() {
        listOf(
            Holding(1L, "Cuenta remunerada ARS", "Cash", "Mercado Pago", 6150.0),
            Holding(2L, "Plazo fijo UVA 90d", "Fixed Income", "Banco Galicia", 11300.0),
            Holding(3L, "AL30D Bonar 2030", "Fixed Income", "Balanz", 9100.0),
            Holding(4L, "S&P 500 Index Fund", "Index Fund", "Balanz", 5100.0),
            Holding(5L, "AAPL", "Equity", "Balanz", 7643.0),
            Holding(6L, "NVDA", "Equity", "Balanz", 10557.0),
            Holding(7L, "Bitcoin", "Crypto", "Nexo", 9200.0),
            Holding(8L, "Ethereum", "Crypto", "Nexo", 5580.0),
            Holding(9L, "Bitcoin", "Crypto", "Binance", 13616.0),
            Holding(10L, "USDT (stable)", "Cash", "Binance", 6004.0)
        ).forEach { save(it) }
    }
}
