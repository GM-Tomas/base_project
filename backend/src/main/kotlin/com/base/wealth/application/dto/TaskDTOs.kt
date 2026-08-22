package com.base.wealth.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateTaskRequest(
    @field:NotNull(message = "El userId es obligatorio")
    val userId: UUID,

    @field:NotBlank(message = "El título no puede estar vacío")
    val title: String,

    val completed: Boolean = false
)

data class UpdateTaskRequest(
    val title: String? = null,
    val completed: Boolean? = null
)
