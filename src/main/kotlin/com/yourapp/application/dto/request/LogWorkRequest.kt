package com.yourapp.application.dto.request

import jakarta.validation.constraints.Min

/**
 * DTO для логирования работы над задачей.
 */
data class LogWorkRequest(

    /**
     * Количество затраченного времени (в условных единицах).
     */
    @field:Min(value = 1, message = "Effort must be at least 1")
    val effort: Int
)
