package com.kille.presentation.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AddChapterRequest(
    @field:NotBlank(message = "Chapter title is required")
    @field:Size(min = 1, max = 200, message = "Chapter title must be between 1 and 200 characters")
    val title: String,

    @field:NotBlank(message = "Chapter text S3 key is required")
    @field:Size(min = 1, max = 1000, message = "Chapter text key must be between 1 and 1000 characters")
    val content: String,

    @field:Size(max = 500, message = "Audio URL cannot exceed 500 characters")
    val audioUrl: String? = null,

    @field:Min(value = 1, message = "Chapter index must be at least 1")
    val index: Int
)