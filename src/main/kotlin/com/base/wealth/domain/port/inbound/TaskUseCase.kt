package com.base.wealth.domain.port.inbound

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import java.util.UUID

interface TaskUseCase {
    fun getAllTasks(userId: UserId): List<Task>

    fun getTaskById(
        userId: UserId,
        id: UUID,
    ): Task

    fun createTask(
        userId: UserId,
        request: CreateTaskRequest,
    ): Task

    fun updateTask(
        userId: UserId,
        id: UUID,
        request: UpdateTaskRequest,
    ): Task

    fun deleteTask(
        userId: UserId,
        id: UUID,
    )
}
