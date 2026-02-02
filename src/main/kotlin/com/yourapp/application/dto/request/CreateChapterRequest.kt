package com.yourapp.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO для создания главы книги.
 *
 * Используется внутри CreateBookRequest для создания глав книги.
 */
data class CreateChapterRequest(

    /**
     * Название главы.
     * Не может быть пустым, должно содержать от 1 до 200 символов.
     */
    @field:NotBlank(message = "Chapter title is required")
    @field:Size(min = 1, max = 200, message = "Chapter title must be between 1 and 200 characters")
    val title: String,

    /**
     * Содержание главы (текст).
     * Не может быть пустым, должно содержать от 1 до 50000 символов.
     */
    @field:NotBlank(message = "Chapter content is required")
    @field:Size(min = 1, max = 50000, message = "Chapter content must be between 1 and 50000 characters")
    val content: String,

    /**
     * URL аудиофайла главы (опционально).
     * Может содержать до 500 символов.
     */
    @field:Size(max = 500, message = "Audio URL cannot exceed 500 characters")
    val audioUrl: String? = null,

    /**
     * Индекс/номер главы в книге.
     * Должен быть положительным числом, начиная с 1.
     * Главы будут отсортированы по этому индексу.
     */
    @field:Min(value = 1, message = "Chapter index must be at least 1")
    val index: Int
)