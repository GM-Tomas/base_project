package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Task
import com.base.wealth.domain.port.outbound.TaskRepository
import com.base.wealth.infrastructure.adapter.outbound.persistence.kv.KvStore
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

private const val PREFIX = "task:"

@Repository
class KvTaskRepository(private val kv: KvStore) : TaskRepository {

    private val mapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    init {
        if (kv.scan(PREFIX).isEmpty()) seed()
    }

    override fun findAll(): List<Task> = kv.scan(PREFIX).values.map { mapper.readValue<Task>(it) }

    override fun findById(id: UUID): Task? = kv.get(PREFIX + id)?.let { mapper.readValue<Task>(it) }

    override fun save(task: Task): Task {
        kv.put(PREFIX + task.id, mapper.writeValueAsString(task))
        return task
    }

    override fun deleteById(id: UUID): Boolean = kv.delete(PREFIX + id)

    // Datos semilla iniciales
    private fun seed() {
        val demoUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        listOf(
            Task(UUID.randomUUID(), demoUserId, "Rebalancear portafolio crypto", false, Instant.now()),
            Task(UUID.randomUUID(), demoUserId, "Revisar vencimiento de plazo fijo UVA", true, Instant.now().minusSeconds(86400)),
            Task(UUID.randomUUID(), demoUserId, "Completar aporte mensual en ETF S&P 500", false, Instant.now())
        ).forEach { save(it) }
    }
}
