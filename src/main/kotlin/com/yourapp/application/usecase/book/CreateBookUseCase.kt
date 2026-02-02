package com.yourapp.application.usecase.book

import com.yourapp.application.dto.request.CreateBookRequest
import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.repository.BookRepository
import com.yourapp.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateBookUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCase<CreateBookRequest, BookResponse> {

    @Transactional
    override fun execute(input: CreateBookRequest): BookResponse {
        // 1. Генерируем ID книги заранее
        val bookId = BookId.generate()

        // 2. Создаем главы с правильным bookId
        val chapters = input.chapters.map { chapterRequest ->
            Chapter.createWithText(
                bookId = bookId,
                index = com.yourapp.domain.model.ChapterIndex(chapterRequest.index),
                title = chapterRequest.title,
                text = chapterRequest.text ?: ""
            )
        }

        // 3. Создание книги с главами
        val book = Book.createWithId(
            id = bookId,
            title = input.title,
            author = input.author,
            language = input.language,
            coverUrl = input.coverUrl,
            chapters = chapters,
            audio = input.audio,
            text = input.text
        )

        // 4. Сохранение через репозиторий
        val savedBook = repository.save(book)

        // 5. Преобразование Domain -> DTO
        return mapper.toResponse(savedBook)
    }
}