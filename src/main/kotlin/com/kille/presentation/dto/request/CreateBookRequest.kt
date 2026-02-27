package com.kille.presentation.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateBookRequest(

    @field:NotBlank(message = "Book title is required")
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String,

    @field:NotBlank(message = "Author is required")
    @field:Size(min = 1, max = 100, message = "Author name must be between 1 and 100 characters")
    val author: String,

    @field:NotBlank(message = "Language is required")
    @field:Size(min = 1, max = 50, message = "Language must be between 1 and 50 characters")
    val language: String,

    @field:Size(max = 500, message = "Cover URL cannot exceed 500 characters")
    val coverUrl: String? = null,

    val audio: Boolean = false,

    val text: Boolean = true,

    @field:NotEmpty(message = "Book must contain at least one chapter")
    @field:Valid
    val chapters: List<ChapterData>
)


data class ChapterData(
    @field:Min(value = 0, message = "Chapter index must be >= 0")
    val index: Int,

    @field:Size(max = 500, message = "Title must be less than 500 characters")
    val title: String? = null,

    val text: String? = null,

    val audioUrl: String? = null
)