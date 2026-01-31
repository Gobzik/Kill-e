package com.yourapp.application.dto.response

import java.time.LocalDateTime

/**
 * DTO для представления ExampleEntity в ответах API.
 *
 * Скрывает внутренние детали доменной модели от клиента.
 */
data class ExampleResponse(
    val id: String,
    val title: String,
    val description: String?,
    val status: String,
    val statusDisplayName: String,
    val priority: String,
    val priorityDisplayName: String,
    val estimatedEffort: Int,
    val actualEffort: Int,
    val progressPercentage: Int,
    val isOverEstimated: Boolean,
    val isCompleted: Boolean,
    val canBeEdited: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?
)
