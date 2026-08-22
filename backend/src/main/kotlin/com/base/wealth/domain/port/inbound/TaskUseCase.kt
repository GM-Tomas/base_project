package com.base.wealth.domain.port.inbound

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import java.util.UUID

interface TaskUseCase {
    fun getAllTasks(userId: UUID? = null): List<Task>
    fun getTaskById(id: UUID): Task
    fun createTask(request: CreateTaskRequest): Task
    fun updateTask(id: UUID, request: UpdateTaskRequest): Task
    fun deleteTask(id: UUID)
}
