package com.yourapp.application.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * DTO для создания книги.
 *
 * Используется для передачи данных от клиента к серверу при создании новой книги.
 * Содержит Bean Validation аннотации для валидации на уровне презентации.
 */
data class CreateBookRequest(

    /**
     * Название книги.
     * Не может быть пустым, должно содержать от 1 до 200 символов.
     */
    @field:NotBlank(message = "Book title is required")
    @field:Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    val title: String,

    /**
     * Автор книги.
     * Не может быть пустым, должно содержать от 1 до 100 символов.
     */
    @field:NotBlank(message = "Author is required")
    @field:Size(min = 1, max = 100, message = "Author name must be between 1 and 100 characters")
    val author: String,

    /**
     * Язык книги.
     * Не может быть пустым, должно содержать от 1 до 50 символов.
     */
    @field:NotBlank(message = "Language is required")
    @field:Size(min = 1, max = 50, message = "Language must be between 1 and 50 characters")
    val language: String,

    /**
     * URL обложки книги (опционально).
     * Может содержать до 500 символов.
     */
    @field:Size(max = 500, message = "Cover URL cannot exceed 500 characters")
    val coverUrl: String? = null,

    /**
     * Флаг наличия аудиоверсии.
     */
    val audio: Boolean = false,

    /**
     * Флаг наличия текстовой версии.
     */
    val text: Boolean = true,

    /**
     * Список глав книги.
     * Должен содержать минимум одну главу.
     * Каждая глава валидируется отдельно.
     */
    @field:NotEmpty(message = "Book must contain at least one chapter")
    @field:Valid
    val chapters: List<CreateChapterRequest>
)