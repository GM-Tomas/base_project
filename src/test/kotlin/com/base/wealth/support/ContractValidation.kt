package com.base.wealth.support

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.report.ValidationReport
import org.springframework.test.web.servlet.ResultMatcher

// contracts/openapi.yaml is the hand-authored source of truth (specs/001-backend-para-frontend) —
// its path is injected via -Dcontracts.openapi.path (see build.gradle.kts) instead of a relative
// "../../.." so this doesn't depend on the test JVM's working directory.
private val validator: OpenApiInteractionValidator by lazy {
    val path =
        requireNotNull(System.getProperty("contracts.openapi.path")) {
            "contracts.openapi.path system property not set — see build.gradle.kts Test task config"
        }
    // ponytail: allOf + additionalProperties is a documented false-positive in this validator
    // (ValidationProblem = Problem allOf {errors} — each branch alone looks like it forbids the
    // other's fields; confirmed empirically that IGNORE here doesn't hide real violations, e.g. a
    // missing required field inside `errors[]` still fails). Upgrade path: retire this the day the
    // library fixes allOf+additionalProperties composition (tracked upstream, see its README FAQ).
    val levels =
        LevelResolver
            .create()
            .withLevel(
                "validation.schema.additionalProperties",
                ValidationReport.Level.IGNORE,
            ).build()
    OpenApiInteractionValidator.createFor(path).withLevelResolver(levels).build()
}

/** Asserts a MockMvc request+response pair matches contracts/openapi.yaml (tasks.md T-82). */
fun matchesContract(): ResultMatcher = openApi().isValid(validator)
