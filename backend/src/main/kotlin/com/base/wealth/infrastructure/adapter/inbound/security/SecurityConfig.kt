package com.base.wealth.infrastructure.adapter.inbound.security

import com.base.wealth.infrastructure.config.WealthProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain

/**
 * `exp`/`nbf`/`iss` (via [JwtValidators.createDefaultWithIssuer]) plus `aud` — Supabase issues
 * `aud: "authenticated"` for logged-in users. Factored out of [SecurityConfig.jwtDecoder] so
 * [JwtValidatorTest] can exercise the validation rules against hand-built [Jwt]s, without a live
 * JWKS call.
 */
fun supabaseTokenValidator(
    issuer: String,
    audience: String,
): OAuth2TokenValidator<Jwt> =
    DelegatingOAuth2TokenValidator(
        JwtValidators.createDefaultWithIssuer(issuer),
        JwtClaimValidator<List<String>>("aud") { audiences -> audiences != null && audience in audiences },
    )

/**
 * Stateless OAuth2 resource server: every request under the /api/v1 prefix must carry a Bearer
 * JWT issued by this project's Supabase Auth (see [jwtDecoder]). No sessions, no login form, no
 * CSRF — there's no cookie-based auth to protect against forgery for
 * (specs/001-backend-para-frontend/plan.md §5.2).
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authEntryPoint: ProblemDetailAuthEntryPoint,
    private val accessDeniedHandler: ProblemDetailAccessDeniedHandler,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors { }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/v1/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2ResourceServer { it.jwt { } }
            .exceptionHandling {
                it
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            }.headers { it.frameOptions { fo -> fo.deny() } }
        return http.build()
    }

    @Bean
    fun jwtDecoder(wealthProperties: WealthProperties): JwtDecoder {
        val auth = wealthProperties.auth
        val decoder = NimbusJwtDecoder.withJwkSetUri(auth.jwkSetUri).build()
        decoder.setJwtValidator(supabaseTokenValidator(auth.issuer, auth.audience))
        return decoder
    }
}
