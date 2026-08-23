import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.24"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
}

group = "com.base.wealth"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core Web & Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // KVS persistence (prod profile talks to the Supabase Postgres kv_store table)
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // Kotlin Support & Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // OpenAPI & Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
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
}

// generateOpenApiDocs has no inputs Gradle can see (its real input is the live HTTP response),
// so without this it gets marked UP-TO-DATE and silently skips re-scraping on every commit.
tasks.named("generateOpenApiDocs") {
    outputs.upToDateWhen { false }
}
