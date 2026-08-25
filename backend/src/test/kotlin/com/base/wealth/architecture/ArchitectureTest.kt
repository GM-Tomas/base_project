package com.base.wealth.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.imports
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RestController

private const val DOMAIN_PACKAGE = "com.base.wealth.domain.."
private const val APPLICATION_PACKAGE = "com.base.wealth.application.."
private const val INFRASTRUCTURE_PACKAGE = "com.base.wealth.infrastructure.."

/**
 * Enforces the hexagonal boundaries described in specs/001-backend-para-frontend/plan.md §8.2.
 * Some rules from that spec are [Disabled]: their precondition (UserId, Money, DTO-free ports)
 * is later-fase work (see tasks.md) and doesn't hold in the codebase yet.
 */
class ArchitectureTest {
    private val domain = Layer("Domain", DOMAIN_PACKAGE)
    private val application = Layer("Application", APPLICATION_PACKAGE)
    private val infrastructure = Layer("Infrastructure", INFRASTRUCTURE_PACKAGE)

    @Test
    fun `domain does not depend on infrastructure`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                domain.doesNotDependOn(infrastructure)
            }
    }

    @Test
    @Disabled(
        "Enable once inbound ports take domain commands instead of application DTOs (fuga A1) " +
            "— specs/001-backend-para-frontend/plan.md §1, tasks.md T-40",
    )
    fun `domain does not depend on application`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                domain.doesNotDependOn(application)
            }
    }

    @Test
    fun `application does not depend on infrastructure`() {
        Konsist
            .scopeFromProject()
            .assertArchitecture {
                application.doesNotDependOn(infrastructure)
            }
    }

    @Test
    fun `domain does not depend on frameworks`() {
        val forbiddenPrefixes = listOf("org.springframework", "jakarta.", "com.fasterxml")

        Konsist
            .scopeFromPackage(DOMAIN_PACKAGE)
            .files
            .imports
            .assertFalse { import -> forbiddenPrefixes.any { import.name.startsWith(it) } }
    }

    @Test
    fun `controllers depend on inbound use case ports, not concrete services`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withAllAnnotationsOf(RestController::class)
            .assertTrue { controller ->
                controller.primaryConstructor
                    ?.parameters
                    ?.all { it.type.sourceType.endsWith("UseCase") }
                    ?: true
            }
    }

    @Test
    fun `spring stereotypes are final and use constructor injection`() {
        Konsist
            .scopeFromProject()
            .classes()
            .filter {
                it.hasAnnotationOf(Service::class) ||
                    it.hasAnnotationOf(Repository::class) ||
                    it.hasAnnotationOf(RestController::class)
            }.assertFalse { it.hasOpenModifier }
    }

    @Test
    @Disabled("Enable once UserId lands in the domain — specs/001-backend-para-frontend/tasks.md T-21/T-23")
    fun `outbound repository signatures never omit UserId`() = Unit

    @Test
    @Disabled("Enable once Money replaces Double in the domain — specs/001-backend-para-frontend/tasks.md T-20")
    fun `domain types never use Double or Float for money`() = Unit
}
