package com.base.wealth

import com.base.wealth.support.PostgresTestBase
import org.junit.jupiter.api.Test

// Needs a real Postgres now (Testcontainers, via PostgresTestBase) — the full context includes
// JdbcHoldingRepository et al., which need a DataSource to even construct. Requires Docker.
class BaseWealthApplicationTests : PostgresTestBase() {
    @Test
    fun contextLoads() {
        // Verifica que el contexto de Spring Boot inicie correctamente, con Postgres real detrás.
    }
}
