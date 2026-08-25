package com.base.wealth.infrastructure.adapter.inbound.security

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

// CA-01.2: a token with the wrong issuer, wrong audience, or that's expired must fail validation.
// No live JWKS call here — see SecurityFilterChainTest for the end-to-end 401 behaviour.
class JwtValidatorTest {
    private val issuer = "https://vffsdgqqyqcbmkehnpxx.supabase.co/auth/v1"
    private val audience = "authenticated"
    private val validator = supabaseTokenValidator(issuer, audience)

    private fun jwt(
        iss: String = issuer,
        aud: List<String> = listOf(audience),
        expiresAt: Instant = Instant.now().plusSeconds(3600),
    ): Jwt =
        Jwt
            .withTokenValue("token")
            .header("alg", "RS256")
            .issuer(iss)
            .audience(aud)
            .subject(UUID.randomUUID().toString())
            .issuedAt(expiresAt.minusSeconds(3600))
            .expiresAt(expiresAt)
            .build()

    @Test
    @DisplayName("a token with the right issuer and audience passes")
    fun validTokenPasses() {
        assertTrue(validator.validate(jwt()).errors.isEmpty())
    }

    @Test
    @DisplayName("a token from a different issuer is rejected")
    fun wrongIssuerFails() {
        assertFalse(validator.validate(jwt(iss = "https://attacker.example/auth/v1")).errors.isEmpty())
    }

    @Test
    @DisplayName("a token without the 'authenticated' audience is rejected")
    fun wrongAudienceFails() {
        assertFalse(validator.validate(jwt(aud = listOf("anon"))).errors.isEmpty())
    }

    @Test
    @DisplayName("an expired token is rejected")
    fun expiredTokenFails() {
        // Well past JwtTimestampValidator's default 60s clock-skew tolerance, not right at its edge.
        assertFalse(validator.validate(jwt(expiresAt = Instant.now().minusSeconds(600))).errors.isEmpty())
    }
}
