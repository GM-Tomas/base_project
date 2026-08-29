package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.TaskUseCase
import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// Candidato a borrado (spec.md D3): sin UI en el frontend. Se mantiene re-scopeado al JWT
// (tasks.md T-80) en vez de borrarlo directamente. @Hidden porque no forma parte del contrato
// publicado en contracts/openapi.yaml — evita que el contract-drift check (T-83) lo marque
// como divergencia.
@Hidden
@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Endpoints para la gestión de tareas de usuario (Supabase compatible)")
class TaskController(
    private val taskUseCase: TaskUseCase,
) {
    @GetMapping
    @Operation(summary = "Lista las tareas del usuario autenticado")
    fun getAllTasks(
        @CurrentUser userId: UUID,
    ): ResponseEntity<List<Task>> = ResponseEntity.ok(taskUseCase.getAllTasks(UserId(userId)))

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una tarea por su UUID")
    fun getTaskById(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<Task> = ResponseEntity.ok(taskUseCase.getTaskById(UserId(userId), id))

    @PostMapping
    @Operation(summary = "Crea una nueva tarea")
    fun createTask(
        @CurrentUser userId: UUID,
        @Valid @RequestBody request: CreateTaskRequest,
    ): ResponseEntity<Task> {
        val created = taskUseCase.createTask(UserId(userId), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualiza el estado o título de una tarea")
    fun updateTask(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateTaskRequest,
    ): ResponseEntity<Task> {
        val updated = taskUseCase.updateTask(UserId(userId), id, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una tarea")
    fun deleteTask(
        @CurrentUser userId: UUID,
        @PathVariable id: UUID,
    ): ResponseEntity<Void> {
        taskUseCase.deleteTask(UserId(userId), id)
        return ResponseEntity.noContent().build()
    }
}
