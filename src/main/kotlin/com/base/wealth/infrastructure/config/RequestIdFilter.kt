package com.base.wealth.infrastructure.config

import com.base.wealth.infrastructure.adapter.inbound.web.error.TRACE_ID_MDC_KEY
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

private const val REQUEST_ID_HEADER = "X-Request-Id"

/**
 * Puts a per-request id in the MDC (so every log line and every problem+json response can be
 * correlated — CA-08.2, CA-08.3) and echoes it back as a response header. Runs before Spring
 * Security so a 401/403 written by the entry points in adapter.inbound.security also gets one.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        MDC.put(TRACE_ID_MDC_KEY, requestId)
        response.setHeader(REQUEST_ID_HEADER, requestId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY)
        }
    }
}
