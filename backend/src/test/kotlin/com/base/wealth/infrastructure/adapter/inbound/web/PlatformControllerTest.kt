package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreatePlatformCommand
import com.base.wealth.domain.port.inbound.PatchPlatformCommand
import com.base.wealth.domain.port.inbound.PlatformUseCase
import com.base.wealth.exception.ResourceInUseException
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAccessDeniedHandler
import com.base.wealth.infrastructure.adapter.inbound.security.ProblemDetailAuthEntryPoint
import com.base.wealth.infrastructure.adapter.inbound.security.SecurityConfig
import com.base.wealth.infrastructure.config.WealthProperties
import com.base.wealth.support.authenticatedAs
import com.base.wealth.support.matchesContract
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
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

@WebMvcTest(PlatformController::class)
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class PlatformControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var platformUseCase: PlatformUseCase

    @Test
    @DisplayName("GET /api/v1/platforms lista las plataformas del usuario")
    fun testGetAllPlatforms() {
        val platform =
            Platform(UserId(UUID.randomUUID()), PlatformName.of("Balanz"), PlatformType.of("Broker"), Instant.now())
        every { platformUseCase.getAllPlatforms(any()) } returns listOf(platform)

        mockMvc
            .perform(get("/api/v1/platforms").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Balanz"))
            .andExpect(jsonPath("$[0].type").value("Broker"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("POST /api/v1/platforms crea una plataforma")
    fun testCreatePlatform() {
        val created =
            Platform(UserId(UUID.randomUUID()), PlatformName.of("Nexo"), PlatformType.of("Exchange"), Instant.now())
        every { platformUseCase.createPlatform(any<CreatePlatformCommand>()) } returns created

        mockMvc
            .perform(
                post("/api/v1/platforms")
                    .with(authenticatedAs())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Nexo","type":"Exchange"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Nexo"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("PATCH /api/v1/platforms/{name} renombra una plataforma")
    fun testPatchPlatform() {
        val updated =
            Platform(
                UserId(UUID.randomUUID()),
                PlatformName.of("Binance US"),
                PlatformType.of("Exchange"),
                Instant.now(),
            )
        every { platformUseCase.patchPlatform(any<PatchPlatformCommand>()) } returns updated

        mockMvc
            .perform(
                patch("/api/v1/platforms/Binance")
                    .with(authenticatedAs())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Binance US"}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Binance US"))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("DELETE /api/v1/platforms/{name} con posiciones asociadas responde 409")
    fun testDeleteInUsePlatformConflicts() {
        every { platformUseCase.deletePlatform(any(), "Balanz") } throws
            ResourceInUseException("Platform 'Balanz' still has 3 holdings")

        mockMvc
            .perform(delete("/api/v1/platforms/Balanz").with(authenticatedAs()))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(matchesContract())
    }

    @Test
    @DisplayName("DELETE /api/v1/platforms/{name} sin posiciones responde 204")
    fun testDeleteEmptyPlatform() {
        every { platformUseCase.deletePlatform(any(), "Nexo") } just runs

        mockMvc
            .perform(delete("/api/v1/platforms/Nexo").with(authenticatedAs()))
            .andExpect(status().isNoContent)
            .andExpect(matchesContract())
    }
}
