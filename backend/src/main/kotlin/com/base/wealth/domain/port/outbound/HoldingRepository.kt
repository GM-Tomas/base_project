package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.Holding

interface HoldingRepository {
    fun findAll(): List<Holding>
    fun findById(id: Long): Holding?
    fun nextId(): Long
    fun save(holding: Holding): Holding
    fun deleteById(id: Long): Boolean
}
