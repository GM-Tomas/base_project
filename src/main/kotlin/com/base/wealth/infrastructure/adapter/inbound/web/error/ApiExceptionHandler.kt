package com.base.wealth.infrastructure.adapter.inbound.web.error

import com.base.wealth.exception.DuplicateResourceException
import com.base.wealth.exception.ResourceInUseException
import com.base.wealth.exception.ResourceNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ProblemDetail =
        problemDetailOf(HttpStatus.NOT_FOUND, "not-found", ex.message)

    @ExceptionHandler(DuplicateResourceException::class, ResourceInUseException::class)
    fun handleConflict(ex: RuntimeException): ProblemDetail =
        problemDetailOf(HttpStatus.CONFLICT, "conflict", ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        val problem = problemDetailOf(HttpStatus.BAD_REQUEST, "validation", "The request has invalid fields")
        problem.setProperty(
            "errors",
            ex.bindingResult.fieldErrors.map {
                mapOf(
                    "field" to it.field,
                    "message" to (it.defaultMessage ?: "invalid"),
                )
            },
        )
        return problem
    }

    // @Validated on a controller class wraps it in an AOP proxy (MethodValidationInterceptor) that
    // validates @RequestParam constraints (e.g. WealthController.getEstimate's @DecimalMin/@Max
    // query params) before the method body runs — violations surface as this, not
    // MethodArgumentNotValidException (that one's for @Valid @RequestBody/@ModelAttribute) (CA-07.6).
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ProblemDetail {
        val problem = problemDetailOf(HttpStatus.BAD_REQUEST, "validation", "The request has invalid parameters")
        problem.setProperty(
            "errors",
            ex.constraintViolations.map {
                mapOf(
                    "field" to it.propertyPath.toString().substringAfterLast('.'),
                    "message" to it.message,
                )
            },
        )
        return problem
    }

    // A domain factory's require() (e.g. ProjectionParams.of, AssetClass.of) rejecting bad input
    // is a client problem, not a server bug — this is the defense-in-depth layer beneath
    // jakarta.validation (plan.md §3.2's "forma" vs "semántica" split).
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail =
        problemDetailOf(HttpStatus.BAD_REQUEST, "bad-request", ex.message)

    // A 500 never echoes ex.message to the client (CA-08.2) — only the traceId, which the caller
    // can hand back for the real detail to be looked up in the server log (CA-08.3).
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Unhandled exception", ex)
        return problemDetailOf(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "internal",
            "An unexpected error occurred. Quote the traceId when reporting it.",
        )
    }
}
