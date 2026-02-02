package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Use Case: Получение книги по ID.
 */
@Service
class GetBookUseCase(
    private val repository: BookRepository
) : UseCase<UUID, BookResponse> {

    @Transactional(readOnly = true)
    override fun execute(bookId: UUID): BookResponse {
        // Используем твой репозиторий
        val book = repository.findById(BookId(bookId))
            ?: throw RuntimeException("Book with ID $bookId not found")

        // Конвертируем Book в BookResponse
        return BookResponse(
            id = book.id.value,
            title = book.title,
            author = book.author,
            language = book.language,
            coverUrl = book.coverUrl,
            hasAudio = book.hasAudio(),
            hasText = book.hasText(),
            chapterCount = book.chapterCount(),
            chapters = book.chapters().map { chapter ->
                ChapterResponse(
                    id = chapter.id.value,
                    title = chapter.title,
                    index = chapter.index,
                    hasAudio = chapter.audioUrl != null,
                    hasText = chapter.content.isNotBlank()
                )
            }
        )
    }
}