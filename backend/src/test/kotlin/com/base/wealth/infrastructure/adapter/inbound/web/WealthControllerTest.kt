package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.EstimateRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class WealthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("GET /api/v1/wealth/summary debe retornar totales y agrupaciones")
    fun testGetSummary() {
        mockMvc.perform(get("/api/v1/wealth/summary"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalNetWorthUSD").isNumber)
            .andExpect(jsonPath("$.totalNetWorthARS").isNumber)
            .andExpect(jsonPath("$.byAssetClass").isArray)
            .andExpect(jsonPath("$.byPlatform").isArray)
    }

    @Test
    @DisplayName("POST /api/v1/wealth/estimate debe calcular proyección y responder con breakdown")
    fun testEstimateEndpoint() {
        val request = EstimateRequest(
            contribution = 1000.0,
            yieldPct = 8.0,
            years = 5,
            initialPrincipal = 50000.0
        )

        mockMvc.perform(
            post("/api/v1/wealth/estimate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalYears").value(5))
            .andExpect(jsonPath("$.yearlyBreakdown.length()").value(5))
            .andExpect(jsonPath("$.totalFutureValue").isNumber)
    }

    @Test
    @DisplayName("GET /api/v1/wealth/history debe retornar historial mensual")
    fun testGetHistory() {
        mockMvc.perform(get("/api/v1/wealth/history"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").isNotEmpty)
            .andExpect(jsonPath("$[0].m").exists())
            .andExpect(jsonPath("$[0].v").isNumber)
    }
}
