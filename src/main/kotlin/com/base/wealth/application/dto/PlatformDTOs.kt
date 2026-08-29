package com.base.wealth.application.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant

data class CreatePlatformRequest(
    @field:NotBlank(message = "El nombre de la plataforma no puede estar vacío")
    val name: String,
    val type: String = "Other",
)

data class PatchPlatformRequest(
    val name: String? = null,
    val type: String? = null,
)

data class PlatformResponse(
    val name: String,
    val type: String,
    val createdAt: Instant,
)
