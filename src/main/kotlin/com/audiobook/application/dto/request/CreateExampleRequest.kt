package com.audiobook.application.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO для создания ExampleEntity.
 *
 * Используется для передачи данных от клиента к серверу.
 * Содержит Bean Validation аннотации для валидации на уровне презентации.
 */
data class CreateExampleRequest(

    /**
     * Название задачи.
     */
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String,

    /**
     * Описание задачи.
     */
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    /**
     * Приоритет (LOW, MEDIUM, HIGH, CRITICAL).
     */
    @field:NotBlank(message = "Priority is required")
    val priority: String,

    /**
     * Оценка сложности (1-100).
     */
    @field:Min(value = 1, message = "Estimated effort must be at least 1")
    @field:Max(value = 100, message = "Estimated effort cannot exceed 100")
    val estimatedEffort: Int
)
