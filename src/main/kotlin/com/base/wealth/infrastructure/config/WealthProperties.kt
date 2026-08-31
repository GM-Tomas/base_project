package com.base.wealth.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "wealth")
data class WealthProperties(
    val cors: Cors = Cors(),
    val auth: Auth = Auth(),
) {
    data class Cors(
        val allowedOrigins: List<String> = listOf("http://localhost:3000", "http://127.0.0.1:3000"),
    )

    // Verified against the Supabase Auth JWT — see security/SecurityConfig.kt.
    data class Auth(
        val jwkSetUri: String = "",
        val issuer: String = "",
        val audience: String = "authenticated",
    )
}
