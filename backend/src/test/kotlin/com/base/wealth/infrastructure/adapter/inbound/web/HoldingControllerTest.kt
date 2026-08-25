package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.HoldingId
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreateHoldingCommand
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.domain.port.inbound.PatchHoldingCommand
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAccessDeniedHandler
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAuthEntryPoint
import com.base.wealth.infrastructure.adapter.inbound.security.SecurityConfig
import com.base.wealth.infrastructure.config.WealthProperties
import com.base.wealth.support.authenticatedAs
import com.base.wealth.support.matchesContract
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

// @WebMvcTest: web layer, service layer mocked — no database needed (see application.yml's
// default-profile comment). @Import(SecurityConfig): @WebMvcTest does NOT auto-detect a custom
// SecurityFilterChain bean on its own — without this, Spring Boot's own default security
// (HTTP Basic, permit nothing) would run instead, silently.
@WebMvcTest(HoldingController::class)
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class HoldingControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var holdingUseCase: HoldingUseCase

    private fun sampleHolding(id: HoldingId = HoldingId.new()) =
        Holding
            .create(
                userId = UserId(UUID.randomUUID()),
                name = "Cuenta remunerada ARS",
                assetClass = AssetClass.of("Cash"),
                platform = PlatformName.of("Mercado Pago"),
                value = Money.of(6150.0),
                now = Instant.now(),
            ).let { it.copy(id = id) }

    @Test
    @DisplayName("GET /api/v1/holdings sin token responde 401")
    fun noTokenIsRejected() {
        mockMvc.perform(get("/api/v1/holdings")).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("GET /api/v1/holdings debe retornar lista de posiciones")
    fun testGetAllHoldings() {
        every { holdingUseCase.getAllHoldings(any(), any(), any()) } returns listOf(sampleHolding())

        mockMvc
            .perform(get("/api/v1/holdings").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Cuenta remunerada ARS"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("GET /api/v1/holdings?assetClass=Crypto filtra por clase")
    fun testFilterByAssetClass() {
        every { holdingUseCase.getAllHoldings(any(), AssetClass.of("Crypto"), null) } returns emptyList()

        mockMvc
            .perform(get("/api/v1/holdings").param("assetClass", "Crypto").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @DisplayName("GET /api/v1/holdings/{id} debe retornar posición existente")
    fun testGetHoldingById() {
        val holding = sampleHolding()
        every { holdingUseCase.getHoldingById(any(), holding.id) } returns holding

        mockMvc
            .perform(get("/api/v1/holdings/${holding.id.value}").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(holding.id.value.toString()))
            .andExpect(jsonPath("$.name").value("Cuenta remunerada ARS"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("POST /api/v1/holdings debe crear una nueva posición correctamente")
    fun testCreateHolding() {
        val created =
            Holding.create(
                userId = UserId(UUID.randomUUID()),
                name = "Solana",
                assetClass = AssetClass.of("Crypto"),
                platform = PlatformName.of("Binance"),
                value = Money.of(2250.0),
                now = Instant.now(),
            )
        every { holdingUseCase.createHolding(any<CreateHoldingCommand>()) } returns created

        mockMvc
            .perform(
                post("/api/v1/holdings")
                    .with(authenticatedAs())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Solana","assetClass":"Crypto","platform":"Binance","valueUsd":2250.0}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Solana"))
            .andExpect(jsonPath("$.valueUsd").value(2250.0))
            .andExpect(jsonPath("$.assetClass").value("Crypto"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("PATCH /api/v1/holdings/{id} actualiza parcialmente una posición")
    fun testPatchHolding() {
        val holding = sampleHolding()
        val patched = holding.copy(value = Money.of(7000.0))
        every { holdingUseCase.updateHolding(any<PatchHoldingCommand>()) } returns patched

        mockMvc
            .perform(
                patch("/api/v1/holdings/${holding.id.value}")
                    .with(authenticatedAs())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"valueUsd":7000.0}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.valueUsd").value(7000.0))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("DELETE /api/v1/holdings/{id} debe eliminar la posición")
    fun testDeleteHolding() {
        val id = HoldingId.new()
        every { holdingUseCase.deleteHolding(any(), id) } returns Unit

        mockMvc
            .perform(delete("/api/v1/holdings/${id.value}").with(authenticatedAs()))
            .andExpect(status().isNoContent)
            .andExpect(matchesContract())
    }
}
