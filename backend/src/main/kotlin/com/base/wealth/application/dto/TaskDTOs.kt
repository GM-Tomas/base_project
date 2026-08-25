package com.base.wealth.application.dto

import jakarta.validation.constraints.NotBlank

data class CreateTaskRequest(
    @field:NotBlank(message = "El título no puede estar vacío")
    val title: String,
    val completed: Boolean = false,
)

data class UpdateTaskRequest(
    val title: String? = null,
    val completed: Boolean? = null,
)
