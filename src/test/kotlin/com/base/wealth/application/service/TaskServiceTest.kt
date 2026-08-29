package com.base.wealth.application.service

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.outbound.TaskRepository
import com.base.wealth.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class TaskServiceTest {
    private val userId = UserId(UUID.randomUUID())
    private val taskRepository = mockk<TaskRepository>()
    private val service = TaskService(taskRepository)

    @Test
    @DisplayName("createTask stamps the task with the caller's userId, never one from the request (T-80)")
    fun createTaskUsesCallerUserId() {
        val saved = slot<Task>()
        every { taskRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.createTask(userId, CreateTaskRequest(title = "Configurar alerta", completed = false))

        assertEquals(userId.value, result.userId)
        assertEquals(userId.value, saved.captured.userId)
    }

    @Test
    @DisplayName("getTaskById throws when the task doesn't belong to this user (repository already scopes by userId)")
    fun getTaskByIdNotFound() {
        val id = UUID.randomUUID()
        every { taskRepository.findById(userId, id) } returns null

        assertThrows(ResourceNotFoundException::class.java) { service.getTaskById(userId, id) }
    }

    @Test
    @DisplayName("updateTask applies only the fields present in the request")
    fun updateTaskPatchesPartially() {
        val id = UUID.randomUUID()
        val existing = Task(id, userId.value, "Rebalancear portafolio", false, Instant.now())
        every { taskRepository.findById(userId, id) } returns existing
        val saved = slot<Task>()
        every { taskRepository.save(capture(saved)) } answers { saved.captured }

        val result = service.updateTask(userId, id, UpdateTaskRequest(completed = true))

        assertEquals("Rebalancear portafolio", result.title)
        assertEquals(true, result.completed)
    }

    @Test
    @DisplayName("deleteTask throws when nothing was deleted")
    fun deleteTaskNotFound() {
        val id = UUID.randomUUID()
        every { taskRepository.deleteById(userId, id) } returns false

        assertThrows(ResourceNotFoundException::class.java) { service.deleteTask(userId, id) }
    }
}
