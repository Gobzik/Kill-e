package com.kille.application.port.input.book

import com.kille.presentation.dto.response.BookResponse
import com.kille.presentation.dto.response.ChapterResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.BookId
import com.kille.domain.repository.BookRepository
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
            chapterCount = book.chapterCount(),
            chapters = book.chapters().map { chapter ->
                ChapterResponse.fromDomain(chapter)
            }
        )
    }
}