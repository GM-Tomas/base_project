package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("GET /api/v1/tasks debe retornar listado de tareas")
    fun testGetTasks() {
        mockMvc.perform(get("/api/v1/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").isNotEmpty)
    }

    @Test
    @DisplayName("POST /api/v1/tasks y PATCH /api/v1/tasks/{id} ciclo completo de tarea")
    fun testCreateAndPatchTask() {
        val userId = UUID.randomUUID()
        val createRequest = CreateTaskRequest(
            userId = userId,
            title = "Configurar alerta de precios",
            completed = false
        )

        val createResult = mockMvc.perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Configurar alerta de precios"))
            .andExpect(jsonPath("$.completed").value(false))
            .andReturn()

        val jsonResponse = createResult.response.contentAsString
        val rootNode = objectMapper.readTree(jsonResponse)
        val taskId = rootNode.get("id").asText()

        // Actualizar tarea a completada
        val patchRequest = UpdateTaskRequest(completed = true)
        mockMvc.perform(
            patch("/api/v1/tasks/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))

        // Eliminar tarea
        mockMvc.perform(delete("/api/v1/tasks/$taskId"))
            .andExpect(status().isNoContent)
    }
}
