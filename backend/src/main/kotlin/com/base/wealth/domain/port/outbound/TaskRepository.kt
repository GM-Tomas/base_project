package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.Task
import java.util.UUID

interface TaskRepository {
    fun findAll(): List<Task>
    fun findById(id: UUID): Task?
    fun save(task: Task): Task
    fun deleteById(id: UUID): Boolean
}
