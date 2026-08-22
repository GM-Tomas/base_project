package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Currency
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
            Holding(1L, "Cuenta remunerada ARS", AssetClass.CASH, "Mercado Pago", null, 6150.0, Currency.ARS, 0.3),
            Holding(2L, "Plazo fijo UVA 90d", AssetClass.FIXED_INCOME, "Banco Galicia", null, 11300.0, Currency.ARS, 1.1),
            Holding(3L, "AL30D Bonar 2030", AssetClass.FIXED_INCOME, "Balanz", null, 9100.0, Currency.USD, -0.6),
            Holding(4L, "S&P 500 Index Fund", AssetClass.INDEX_FUND, "Balanz", 42.0, 5100.0, Currency.USD, 2.4),
            Holding(5L, "AAPL", AssetClass.EQUITY, "Balanz", 34.0, 7643.0, Currency.USD, 1.8),
            Holding(6L, "NVDA", AssetClass.EQUITY, "Balanz", 8.0, 10557.0, Currency.USD, 4.2),
            Holding(7L, "Bitcoin", AssetClass.CRYPTO, "Nexo", 0.098, 9200.0, Currency.BTC, 6.1),
            Holding(8L, "Ethereum", AssetClass.CRYPTO, "Nexo", 1.62, 5580.0, Currency.ETH, -2.3),
            Holding(9L, "Bitcoin", AssetClass.CRYPTO, "Binance", 0.145, 13616.0, Currency.BTC, 6.1),
            Holding(10L, "USDT (stable)", AssetClass.CASH, "Binance", 6004.0, 6004.0, Currency.USDT, 0.0)
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
