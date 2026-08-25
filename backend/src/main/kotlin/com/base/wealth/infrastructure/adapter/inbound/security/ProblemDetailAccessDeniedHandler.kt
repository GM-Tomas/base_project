package com.base.wealth.infrastructure.adapter.inbound.security

import com.base.wealth.infrastructure.adapter.inbound.web.error.problemDetailOf
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/** Same rationale as [ProblemDetailAuthEntryPoint]: this runs in the filter chain, not in Spring MVC. */
@Component
class ProblemDetailAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val problem = problemDetailOf(HttpStatus.FORBIDDEN, "forbidden", "You don't have access to this resource")
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        objectMapper.writeValue(response.writer, problem)
    }
}
