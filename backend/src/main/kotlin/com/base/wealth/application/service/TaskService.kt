package com.base.wealth.application.service

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.port.inbound.TaskUseCase
import com.base.wealth.domain.port.outbound.TaskRepository
import com.base.wealth.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository
) : TaskUseCase {

    override fun getAllTasks(userId: UUID?): List<Task> {
        val list = taskRepository.findAll()
        return if (userId != null) {
            list.filter { it.userId == userId }.sortedByDescending { it.createdAt }
        } else {
            list.sortedByDescending { it.createdAt }
        }
    }

    override fun getTaskById(id: UUID): Task =
        taskRepository.findById(id) ?: throw ResourceNotFoundException("No se encontró la tarea con ID: $id")

    override fun createTask(request: CreateTaskRequest): Task {
        val task = Task(
            id = UUID.randomUUID(),
            userId = request.userId,
            title = request.title,
            completed = request.completed,
            createdAt = Instant.now()
        )
        return taskRepository.save(task)
    }

    override fun updateTask(id: UUID, request: UpdateTaskRequest): Task {
        val existing = getTaskById(id)
        val updated = existing.copy(
            title = request.title ?: existing.title,
            completed = request.completed ?: existing.completed
        )
        return taskRepository.save(updated)
    }

    override fun deleteTask(id: UUID) {
        if (!taskRepository.deleteById(id)) {
            throw ResourceNotFoundException("No se encontró la tarea con ID: $id para eliminar")
        }
    }
}
