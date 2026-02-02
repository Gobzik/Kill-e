package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetBookUseCase(
    private val repository: BookRepository
) : UseCase<UUID, BookResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): BookResponse {
        val book = repository.findById(BookId(input))
            ?: throw RuntimeException("Book with ID $input not found")

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
                ChapterResponse.fromDomain(chapter)
            }
        )
    }
}