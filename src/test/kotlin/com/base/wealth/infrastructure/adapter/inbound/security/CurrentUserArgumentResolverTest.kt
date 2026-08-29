package com.base.wealth.infrastructure.adapter.inbound.security

import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class CurrentUserArgumentResolverTest {
    private val resolver = CurrentUserArgumentResolver()

    @AfterEach
    fun clearContext() = SecurityContextHolder.clearContext()

    @Test
    @DisplayName("resolves to the JWT's subject claim")
    fun resolvesFromJwtSubject() {
        val userId = UUID.randomUUID()
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        SecurityContextHolder.getContext().authentication = TestingAuthenticationToken(jwt, null)

        val resolved = resolver.resolveArgument(mockk(relaxed = true), null, mockk(relaxed = true), null)

        assertEquals(userId, resolved)
    }
}
