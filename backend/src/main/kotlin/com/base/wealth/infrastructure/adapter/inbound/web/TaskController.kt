package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.CreateTaskRequest
import com.base.wealth.application.dto.UpdateTaskRequest
import com.base.wealth.domain.model.Task
import com.base.wealth.domain.port.inbound.TaskUseCase
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Endpoints para la gestión de tareas de usuario (Supabase compatible)")
class TaskController(
    private val taskUseCase: TaskUseCase
) {

    @GetMapping
    @Operation(summary = "Lista todas las tareas, opcionalmente filtradas por userId")
    fun getAllTasks(
        @RequestParam(required = false) userId: UUID?
    ): ResponseEntity<List<Task>> {
        return ResponseEntity.ok(taskUseCase.getAllTasks(userId))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una tarea por su UUID")
    fun getTaskById(@PathVariable id: UUID): ResponseEntity<Task> {
        return ResponseEntity.ok(taskUseCase.getTaskById(id))
    }

    @PostMapping
    @Operation(summary = "Crea una nueva tarea")
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<Task> {
        val created = taskUseCase.createTask(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualiza el estado o título de una tarea")
    fun updateTask(
        @PathVariable id: UUID,
        @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<Task> {
        val updated = taskUseCase.updateTask(id, request)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una tarea")
    fun deleteTask(@PathVariable id: UUID): ResponseEntity<Void> {
        taskUseCase.deleteTask(id)
        return ResponseEntity.noContent().build()
    }
}
