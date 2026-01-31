package com.yourapp.application.usecase.book

import com.yourapp.application.dto.request.CreateBookRequest
import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.Book
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.repository.BookRepository
import com.yourapp.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Use Case: Создание новой книги.
 *
 * Application Layer координирует бизнес-логику:
 * 1. Валидация входных данных
 * 2. Создание доменной модели
 * 3. Сохранение через репозиторий
 * 4. Преобразование в DTO
 */
@Service
class CreateBookUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCase<CreateBookRequest, BookResponse> {

    @Transactional
    override fun execute(input: CreateBookRequest): BookResponse {
        // 1. Создание глав из DTO
        val chapters = input.chapters.map { chapterRequest ->
            Chapter.create(
                title = chapterRequest.title,
                content = chapterRequest.content,
                index = chapterRequest.index,
                audioUrl = chapterRequest.audioUrl
            )
        }

        // 2. Создание доменной модели через фабричный метод
        val book = Book.create(
            title = input.title,
            author = input.author,
            language = input.language,
            coverUrl = input.coverUrl,
            chapters = chapters,
            audio = input.audio,
            text = input.text
        )

        // 3. Сохранение через репозиторий
        val savedBook = repository.save(book)

        // 4. Преобразование Domain -> DTO
        return mapper.toResponse(savedBook)
    }
}