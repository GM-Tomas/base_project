package com.base.wealth.infrastructure.adapter.outbound.persistence

import com.base.wealth.domain.model.Task
import com.base.wealth.domain.port.outbound.TaskRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryTaskRepository : TaskRepository {

    private val tasksMap = ConcurrentHashMap<UUID, Task>()

    init {
        // Datos semilla iniciales
        val demoUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val sampleTasks = listOf(
            Task(UUID.randomUUID(), demoUserId, "Rebalancear portafolio crypto", false, Instant.now()),
            Task(UUID.randomUUID(), demoUserId, "Revisar vencimiento de plazo fijo UVA", true, Instant.now().minusSeconds(86400)),
            Task(UUID.randomUUID(), demoUserId, "Completar aporte mensual en ETF S&P 500", false, Instant.now())
        )
        sampleTasks.forEach { tasksMap[it.id] = it }
    }

    override fun findAll(): List<Task> = tasksMap.values.toList()

    override fun findById(id: UUID): Task? = tasksMap[id]

    override fun save(task: Task): Task {
        tasksMap[task.id] = task
        return task
    }

    override fun deleteById(id: UUID): Boolean = tasksMap.remove(id) != null
}
