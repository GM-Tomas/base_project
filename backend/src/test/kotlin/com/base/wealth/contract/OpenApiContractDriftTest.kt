package com.base.wealth.contract

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

/**
 * Runs only via `./gradlew contractDriftCheck` (see build.gradle.kts), never as part of the plain
 * `test` task: it needs docs/api/openapi.json freshly regenerated from the running app first
 * (`generateOpenApiDocs`), which `test` doesn't do. Tasks endpoints are `@Hidden` (see
 * TaskController) precisely so they don't show up here — Tasks isn't part of the published
 * contract (spec.md D3).
 */
@Tag("contract-drift")
class OpenApiContractDriftTest {
    @Test
    fun `el contrato publicado no diverge de los endpoints generados`() {
        val contract = parse(System.getProperty("contracts.openapi.path"))
        val generated = parse(System.getProperty("generated.openapi.path"))

        assertEquals(
            operationKeys(contract),
            operationKeys(generated),
            "contracts/openapi.yaml y docs/api/openapi.json divergen — actualizá el que quedó atrás",
        )
    }

    private fun parse(path: String?): OpenAPI {
        val resolvedPath = requireNotNull(path) { "system property not set — see build.gradle.kts" }
        return requireNotNull(OpenAPIV3Parser().read(resolvedPath)) { "No se pudo parsear $resolvedPath" }
    }

    private fun operationKeys(spec: OpenAPI): Set<String> =
        spec.paths
            .orEmpty()
            .flatMap { (path, item) -> item.readOperationsMap().keys.map { method -> "$method $path" } }
            .toSet()
}
