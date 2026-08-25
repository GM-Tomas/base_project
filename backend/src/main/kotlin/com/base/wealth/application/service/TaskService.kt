package com.base.wealth.application.service

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.TaskUseCase
import com.base.wealth.domain.port.outbound.TaskRepository
import com.base.wealth.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
) : TaskUseCase {
    override fun getAllTasks(userId: UserId): List<Task> = taskRepository.findAll(userId)

    override fun getTaskById(
        userId: UserId,
        id: UUID,
    ): Task =
        taskRepository.findById(userId, id)
            ?: throw ResourceNotFoundException("No se encontró la tarea con ID: $id")

    override fun createTask(
        userId: UserId,
        request: CreateTaskRequest,
    ): Task {
        val task =
            Task(
                id = UUID.randomUUID(),
                userId = userId.value,
                title = request.title,
                completed = request.completed,
                createdAt = Instant.now(),
            )
        return taskRepository.save(task)
    }

    override fun updateTask(
        userId: UserId,
        id: UUID,
        request: UpdateTaskRequest,
    ): Task {
        val existing = getTaskById(userId, id)
        val updated =
            existing.copy(
                title = request.title ?: existing.title,
                completed = request.completed ?: existing.completed,
            )
        return taskRepository.save(updated)
    }

    override fun deleteTask(
        userId: UserId,
        id: UUID,
    ) {
        if (!taskRepository.deleteById(userId, id)) {
            throw ResourceNotFoundException("No se encontró la tarea con ID: $id para eliminar")
        }
    }
}
