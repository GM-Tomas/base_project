package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.port.inbound.TaskUseCase
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAccessDeniedHandler
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAuthEntryPoint
import com.base.wealth.infrastructure.adapter.inbound.security.SecurityConfig
import com.base.wealth.infrastructure.config.WealthProperties
import com.base.wealth.support.authenticatedAs
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

// @Import(SecurityConfig): see HoldingControllerTest for why this is required, not optional.
@WebMvcTest(TaskController::class)
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var taskUseCase: TaskUseCase

    @Test
    @DisplayName("GET /api/v1/tasks sin token responde 401")
    fun noTokenIsRejected() {
        mockMvc.perform(get("/api/v1/tasks")).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("GET /api/v1/tasks debe retornar el listado del usuario autenticado")
    fun testGetTasks() {
        val task = Task(UUID.randomUUID(), UUID.randomUUID(), "Rebalancear portafolio", false, Instant.now())
        every { taskUseCase.getAllTasks(any()) } returns listOf(task)

        mockMvc
            .perform(get("/api/v1/tasks").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    @DisplayName("POST /api/v1/tasks y PATCH /api/v1/tasks/{id} ciclo completo de tarea")
    fun testCreateAndPatchTask() {
        val taskId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val createRequest = CreateTaskRequest(title = "Configurar alerta de precios", completed = false)
        val created = Task(taskId, userId, "Configurar alerta de precios", false, Instant.now())
        val patched = created.copy(completed = true)

        every { taskUseCase.createTask(any(), createRequest) } returns created
        every { taskUseCase.updateTask(any(), taskId, UpdateTaskRequest(completed = true)) } returns patched
        every { taskUseCase.deleteTask(any(), taskId) } returns Unit

        mockMvc
            .perform(
                post("/api/v1/tasks")
                    .with(authenticatedAs(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Configurar alerta de precios"))
            .andExpect(jsonPath("$.completed").value(false))

        mockMvc
            .perform(
                patch("/api/v1/tasks/$taskId")
                    .with(authenticatedAs(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(UpdateTaskRequest(completed = true))),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))

        mockMvc
            .perform(delete("/api/v1/tasks/$taskId").with(authenticatedAs(userId)))
            .andExpect(status().isNoContent)
    }
}
