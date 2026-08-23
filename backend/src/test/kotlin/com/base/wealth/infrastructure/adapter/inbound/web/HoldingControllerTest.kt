package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateHoldingRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class HoldingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("GET /api/v1/holdings debe retornar lista de posiciones iniciales")
    fun testGetAllHoldings() {
        mockMvc.perform(get("/api/v1/holdings"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").isNotEmpty)
            .andExpect(jsonPath("$[0].name").exists())
    }

    @Test
    @DisplayName("GET /api/v1/holdings/{id} debe retornar posición existente")
    fun testGetHoldingById() {
        mockMvc.perform(get("/api/v1/holdings/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Cuenta remunerada ARS"))
    }

    @Test
    @DisplayName("POST /api/v1/holdings debe crear una nueva posición correctamente")
    fun testCreateHolding() {
        val newHolding = CreateHoldingRequest(
            name = "Solana",
            cls = "Crypto",
            platform = "Binance",
            value = 2250.0
        )

        mockMvc.perform(
            post("/api/v1/holdings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newHolding))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Solana"))
            .andExpect(jsonPath("$.value").value(2250.0))
            .andExpect(jsonPath("$.cls").value("Crypto"))
    }

    @Test
    @DisplayName("DELETE /api/v1/holdings/{id} debe eliminar la posición")
    fun testDeleteHolding() {
        mockMvc.perform(delete("/api/v1/holdings/2"))
            .andExpect(status().isNoContent)

        // Verificar que ya no existe
        mockMvc.perform(get("/api/v1/holdings/2"))
            .andExpect(status().isNotFound)
    }
}
