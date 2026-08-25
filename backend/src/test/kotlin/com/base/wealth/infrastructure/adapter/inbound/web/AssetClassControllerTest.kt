package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.domain.port.inbound.AssetClassUseCase
import com.base.wealth.domain.port.inbound.AvailableAssetClasses
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AssetClassController::class)
@Import(SecurityConfig::class, ProblemDetailAuthEntryPoint::class, ProblemDetailAccessDeniedHandler::class)
@EnableConfigurationProperties(WealthProperties::class)
class AssetClassControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var assetClassUseCase: AssetClassUseCase

    @Test
    @DisplayName("GET /api/v1/asset-classes retorna defaults, en uso, y la unión")
    fun testGetAvailableAssetClasses() {
        every { assetClassUseCase.getAvailableAssetClasses(any()) } returns
            AvailableAssetClasses(
                defaults = listOf("Cash", "Equity"),
                inUse = listOf("Cash", "Real Estate"),
                all = listOf("Cash", "Equity", "Real Estate"),
            )

        mockMvc
            .perform(get("/api/v1/asset-classes").with(authenticatedAs()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.defaults.length()").value(2))
            .andExpect(jsonPath("$.all", org.hamcrest.Matchers.hasItem("Real Estate")))
            .andExpect(matchesContract())
    }
}
