package com.yourapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddChapterRequest(
    @field:NotBlank(message = "Chapter title is required")
    @field:Size(min = 1, max = 200, message = "Chapter title must be between 1 and 200 characters")
    val title: String,

    @field:NotBlank(message = "Chapter content is required")
    @field:Size(min = 1, max = 50000, message = "Chapter content must be between 1 and 50000 characters")
    val content: String,

    @field:Size(max = 500, message = "Audio URL cannot exceed 500 characters")
    val audioUrl: String? = null,

    @field:Min(value = 1, message = "Chapter index must be at least 1")
    val index: Int
)