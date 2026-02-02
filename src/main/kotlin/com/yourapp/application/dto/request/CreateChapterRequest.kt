package com.yourapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChapterRequest(
    @field:NotBlank(message = "Book ID is required")
    val bookId: String,

    @field:Min(value = 0, message = "Chapter index must be >= 0")
    val index: Int,

    @field:Size(max = 500, message = "Title must be less than 500 characters")
    val title: String? = null,

    val text: String? = null,

    val audioUrl: String? = null,

    val timingUrl: String? = null,

    @field:Min(value = 1, message = "Duration must be positive")
    val durationMs: Long? = null
) {
    fun validate(): Boolean {
        return text != null || audioUrl != null
    }
}
