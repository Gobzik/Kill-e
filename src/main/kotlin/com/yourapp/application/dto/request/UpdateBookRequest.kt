package com.yourapp.application.dto.request

import jakarta.validation.constraints.Size

data class UpdateBookRequest(
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String? = null,

    @field:Size(min = 1, max = 100, message = "Author name must be between 1 and 100 characters")
    val author: String? = null,

    @field:Size(min = 1, max = 50, message = "Language must be between 1 and 50 characters")
    val language: String? = null,

    @field:Size(max = 500, message = "Cover URL cannot exceed 500 characters")
    val coverUrl: String? = null
)
