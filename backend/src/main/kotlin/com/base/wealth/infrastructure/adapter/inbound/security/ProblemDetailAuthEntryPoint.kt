package com.base.wealth.infrastructure.adapter.inbound.security

import com.base.wealth.infrastructure.adapter.inbound.web.error.problemDetailOf
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * Runs inside the security filter chain, before Spring MVC's exception handling exists — a
 * missing/invalid token never reaches [com.base.wealth.infrastructure.adapter.inbound.web.error.ApiExceptionHandler],
 * so the problem+json body is written here directly (CA-01.1, CA-01.2).
 */
@Component
class ProblemDetailAuthEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val problem = problemDetailOf(HttpStatus.UNAUTHORIZED, "unauthorized", "Missing or invalid access token")
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(response.writer, problem)
    }
}
