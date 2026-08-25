package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import java.util.UUID

interface TaskRepository {
    fun findAll(userId: UserId): List<Task>

    fun findById(
        userId: UserId,
        id: UUID,
    ): Task?

    fun save(task: Task): Task

    fun deleteById(
        userId: UserId,
        id: UUID,
    ): Boolean
}
