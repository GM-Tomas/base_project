package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.port.outbound.HoldingRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryHoldingRepository : HoldingRepository {

    private val holdingsMap = ConcurrentHashMap<Long, Holding>()
    private val idCounter = AtomicLong(10)

    init {
        // Datos semilla alineados con el frontend BASE Wealth
        val initialData = listOf(
            Holding(1L, "Cuenta remunerada ARS", "Cash", "Mercado Pago", null, 6150.0),
            Holding(2L, "Plazo fijo UVA 90d", "Fixed Income", "Banco Galicia", null, 11300.0),
            Holding(3L, "AL30D Bonar 2030", "Fixed Income", "Balanz", null, 9100.0),
            Holding(4L, "S&P 500 Index Fund", "Index Fund", "Balanz", 42.0, 5100.0),
            Holding(5L, "AAPL", "Equity", "Balanz", 34.0, 7643.0),
            Holding(6L, "NVDA", "Equity", "Balanz", 8.0, 10557.0),
            Holding(7L, "Bitcoin", "Crypto", "Nexo", 0.098, 9200.0),
            Holding(8L, "Ethereum", "Crypto", "Nexo", 1.62, 5580.0),
            Holding(9L, "Bitcoin", "Crypto", "Binance", 0.145, 13616.0),
            Holding(10L, "USDT (stable)", "Cash", "Binance", 6004.0, 6004.0)
        )
        initialData.forEach { holdingsMap[it.id] = it }
    }

    override fun findAll(): List<Holding> = holdingsMap.values.sortedBy { it.id }

    override fun findById(id: Long): Holding? = holdingsMap[id]

    override fun nextId(): Long = idCounter.incrementAndGet()

    override fun save(holding: Holding): Holding {
        holdingsMap[holding.id] = holding
        return holding
    }

    override fun deleteById(id: Long): Boolean = holdingsMap.remove(id) != null
}
