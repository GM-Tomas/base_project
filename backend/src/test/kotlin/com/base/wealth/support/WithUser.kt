package com.base.wealth.support

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor
import java.util.UUID

/**
 * Attaches a mock authenticated JWT to a MockMvc request:
 * `mockMvc.perform(get("/x").with(authenticatedAs()))`.
 * Bypasses the real decoder/JWKS entirely (that's exercised by
 * [com.base.wealth.infrastructure.adapter.inbound.security.JwtValidatorTest] instead), so
 * @WebMvcTest/@SpringBootTest slices can authenticate in one line without a live Supabase call.
 */
fun authenticatedAs(userId: UUID = UUID.randomUUID()): RequestPostProcessor =
    jwt().jwt { it.subject(userId.toString()).claim("aud", "authenticated") }
