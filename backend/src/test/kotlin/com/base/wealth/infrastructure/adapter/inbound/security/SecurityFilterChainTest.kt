package com.base.wealth.infrastructure.adapter.inbound.security

import com.base.wealth.domain.model.AssetClass
import com.base.wealth.domain.model.Holding
import com.base.wealth.domain.model.Money
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.HoldingUseCase
import com.base.wealth.infrastructure.adapter.inbound.web.HealthController
import com.base.wealth.infrastructure.adapter.inbound.web.HoldingController
import com.base.wealth.infrastructure.config.WealthProperties
import com.base.wealth.support.authenticatedAs
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

// CA-01.1: every endpoint under /api/v1 except health requires a token — CA-01.2's issuer/
// audience validation logic itself lives in JwtValidatorTest (a real JWKS call is out of scope
// for a unit/slice test). @WebMvcTest does NOT auto-detect a custom SecurityFilterChain bean —
// without this @Import, Spring Boot falls back to its own default (HTTP Basic, permit nothing),
// silently exercising the wrong config.
@WebMvcTest(controllers = [HoldingController::class, HealthController::class])
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class SecurityFilterChainTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var holdingUseCase: HoldingUseCase

    @Test
    @DisplayName("GET /api/v1/holdings without a token returns 401 problem+json")
    fun noTokenIsRejected() {
        mockMvc
            .perform(get("/api/v1/holdings"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.traceId").exists())
    }

    @Test
    @DisplayName("GET /api/v1/health needs no token")
    fun healthIsPublic() {
        mockMvc
            .perform(get("/api/v1/health"))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("A valid token reaches the controller")
    fun validTokenIsAccepted() {
        every { holdingUseCase.getAllHoldings(any()) } returns
            listOf(
                Holding.create(
                    userId = UserId(UUID.randomUUID()),
                    name = "AAPL",
                    assetClass = AssetClass.of("Equity"),
                    platform = PlatformName.of("Balanz"),
                    value = Money.of(100.0),
                    now = Instant.now(),
                ),
            )

        mockMvc
            .perform(get("/api/v1/holdings").with(authenticatedAs()))
            .andExpect(status().isOk)
    }
}
