package com.kille.presentation.dto.request

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

    @field:NotBlank(message = "Text S3 key is required")
    @field:Size(max = 1000, message = "Text S3 key cannot exceed 1000 characters")
    val text: String,

    val audioUrl: String? = null,

    val timingUrl: String? = null
) {
    fun validate(): Boolean {
        return text.isNotBlank()
    }
}
