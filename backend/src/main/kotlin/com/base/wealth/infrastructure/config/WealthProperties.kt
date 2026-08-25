package com.base.wealth.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "wealth")
data class WealthProperties(
    val defaultFxUsdArs: Double = 1050.0,
    val cors: Cors = Cors(),
    val auth: Auth = Auth(),
    // Server-side config, not a hardcoded constant (CA-05.6) — classes outside this set,
    // including ones a user invents (F10), count as illiquid. See domain.service.LiquidityPolicy.
    val liquidAssetClasses: List<String> = listOf("Cash", "Equity", "Crypto", "Index Fund"),
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
