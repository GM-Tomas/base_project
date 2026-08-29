import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    // Pinned to .23, not the newer .24: detekt's Gradle plugin embeds a Kotlin compiler and
    // refuses to run against a project on a different Kotlin version, and no detekt release
    // targets 1.9.24 specifically (1.23.6 is the last on the 1.9.x line, built against 1.9.23;
    // 1.23.7+ moved to Kotlin 2.0). Revisit together with a detekt bump once one lands on 2.0.
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    id("com.diffplug.spotless") version "7.0.4"
    id("io.gitlab.arturbosch.detekt") version "1.23.6" // last release built against Kotlin 1.9.x; 1.23.7+ moved to 2.0
    jacoco
}

group = "com.base.wealth"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core Web & Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Resource server: validates the Supabase Auth JWT on every request (see security/SecurityConfig.kt)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Postgres persistence for every Jdbc*Repository (infrastructure/adapter/outbound/persistence).
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // Schema migrations (versions managed by the Spring Boot BOM above).
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Auto-starts compose.yaml's Postgres for `bootRun`; stripped from the prod jar
    // (developmentOnly), so it has zero footprint outside a developer's machine.
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Kotlin Support & Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // OpenAPI & Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("com.ninja-squad:springmockk:4.0.2")

    // Integration tests against a real, ephemeral Postgres (see support/PostgresTestBase.kt)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // Architecture tests (see architecture/ArchitectureTest.kt)
    testImplementation("com.lemonappdev:konsist:0.17.3")

    // Contract tests: validates real controller responses against contracts/openapi.yaml
    // (see support/ContractValidation.kt, tasks.md T-82) and pulls in swagger-parser, reused by
    // OpenApiContractDriftTest (T-83) to also parse docs/api/openapi.json — no separate YAML/JSON
    // parsing dependency needed for that.
    testImplementation("com.atlassian.oai:swagger-request-validator-mockmvc:2.46.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
        allWarningsAsErrors = true
    }
}

// Deliberately tasks.test { ... }, not tasks.withType<Test> { ... }: the latter would also
// configure contractDriftCheck below (also a Test task), stacking a contradictory
// includeTags+excludeTags("contract-drift") on it and dragging in jacocoTestReport → the full
// Docker-dependent `test` task as a side effect of running contractDriftCheck alone.
tasks.test {
    useJUnitPlatform {
        // OpenApiContractDriftTest needs docs/api/openapi.json freshly regenerated first
        // (see the contractDriftCheck task below) — it would false-fail on a stale or absent
        // file if it ran as part of the plain `test` task.
        excludeTags("contract-drift")
    }
    finalizedBy(tasks.jacocoTestReport)
    // contracts/openapi.yaml lives outside this Gradle project's source sets (specs/ is a sibling
    // of build.gradle.kts, not under src/), so tests that validate against it
    // (ContractValidation.kt, T-82) need an absolute path rather than one relative to whatever the
    // test JVM's working directory happens to be.
    systemProperty(
        "contracts.openapi.path",
        file("specs/001-backend-para-frontend/contracts/openapi.yaml").absolutePath,
    )
}

// Compares contracts/openapi.yaml (hand-authored source of truth) against docs/api/openapi.json
// (generated from the running app's /v3/api-docs) at the path+method level — catches an endpoint
// added/removed on one side without the other (tasks.md T-83, plan.md §8.4). Deliberately not a
// deep schema diff: the two are produced by different tools with different-but-equivalent
// shapes, so a byte/structural diff would flag noise, not real drift; the per-response-body
// contract test (T-82) already covers the fields shape for the paths that matter.
tasks.register<Test>("contractDriftCheck") {
    description = "Compara contracts/openapi.yaml contra docs/api/openapi.json generado (tasks.md T-83)."
    group = "verification"
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags("contract-drift") }
    dependsOn("generateOpenApiDocs")
    systemProperty(
        "contracts.openapi.path",
        file("specs/001-backend-para-frontend/contracts/openapi.yaml").absolutePath,
    )
    systemProperty("generated.openapi.path", file("docs/api/openapi.json").absolutePath)
}

// `./gradlew bootRun` runs as the `dev` profile by default (Postgres via compose.yaml);
// override with `-Dspring.profiles.active=prod` to exercise the prod wiring locally.
// `./gradlew test` is untouched by this — it only configures the bootRun task.
tasks.named<BootRun>("bootRun") {
    systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active", "dev"))
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt.yml"))
    baseline = file("config/detekt-baseline.xml")
    source.setFrom(files("src/main/kotlin", "src/test/kotlin"))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "21"
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/dto/**",
                        // ponytail: domain/model + domain/port are still plain data classes and DTO-shaped
                        // inbound/outbound interfaces (Fase 0) — no logic to cover, just generated
                        // equals/hashCode/copy and interface default-parameter stubs dragging the ratio
                        // down. Drop this exclusion once Fase 2 lands real domain types (tasks.md T-20-23);
                        // application.service (the one package here with actual logic) stays measured.
                        "**/domain/model/**",
                        "**/domain/port/**",
                    )
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
    violationRules {
        rule {
            element = "PACKAGE"
            includes = listOf("com.base.wealth.domain.*", "com.base.wealth.application.*")
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Regenerates docs/api/openapi.json from the running app's /v3/api-docs (see .githooks/pre-commit)
openApi {
    apiDocsUrl.set("http://localhost:8080/v3/api-docs")
    outputDir.set(file("docs/api"))
    outputFileName.set("openapi.json")
}

// ponytail: forkedSpringBootRun hashes its whole workingDir for up-to-date checks and trips
// on Windows file locks held by the Gradle daemon itself (springdoc-openapi-gradle-plugin#106).
// Untracked state is fine here — this task's only job is to boot the app once and shut it down.
tasks.named("forkedSpringBootRun") {
    doNotTrackState("Forks the app only to scrape /v3/api-docs; not meaningfully cacheable")
    // The plugin puts the full runtime classpath (including test output) on this task without
    // declaring that dependency itself — Gradle 8.7's task validation flags the resulting
    // implicit ordering. Declaring it here is the fix Gradle's own error message suggests.
    mustRunAfter(
        tasks.named("compileTestKotlin"),
        tasks.named("compileTestJava"),
        tasks.named("processTestResources"),
    )
}

// generateOpenApiDocs has no inputs Gradle can see (its real input is the live HTTP response),
// so without this it gets marked UP-TO-DATE and silently skips re-scraping on every commit.
tasks.named("generateOpenApiDocs") {
    outputs.upToDateWhen { false }
}
