package com.base.wealth.infrastructure.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private const val USER_ID_MDC_KEY = "userId"

/**
 * Adds the authenticated user's id to the MDC — never the token or the email (CA-08.3). Ordered
 * to run right after Spring Security's filter chain, once the JWT has actually been verified.
 */
@Component
@Order(SecurityProperties.DEFAULT_FILTER_ORDER + 1)
class UserContextMdcFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val jwt = SecurityContextHolder.getContext().authentication?.principal as? Jwt
        if (jwt != null) MDC.put(USER_ID_MDC_KEY, jwt.subject)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(USER_ID_MDC_KEY)
        }
    }
}
