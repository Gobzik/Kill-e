package com.kille.application.port.input.book

import com.kille.presentation.dto.response.BookResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.Book
import com.kille.domain.model.BookId
import com.kille.domain.repository.BookRepository
import com.kille.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateBookUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCase<UpdateBookCommand, BookResponse> {

    @Transactional
    override fun execute(input: UpdateBookCommand): BookResponse {
        val book = repository.findById(BookId(input.bookId))
            ?: throw RuntimeException("Book with ID ${input.bookId} not found")

        val chapters = book.chapters()
        val hasAudio = chapters.any { it.hasAudio() }
        val hasText = chapters.any { it.hasText() }

        val updatedBook = Book.restore(
            id = book.id,
            title = input.title ?: book.title,
            author = input.author ?: book.author,
            language = input.language ?: book.language,
            coverUrl = input.coverUrl ?: book.coverUrl,
            chapters = chapters,
            audio = hasAudio,
            text = hasText
        )
        val savedBook = repository.save(updatedBook)
        return mapper.toResponse(savedBook)
    }
}

data class UpdateBookCommand(
    val bookId: UUID,
    val title: String? = null,
    val author: String? = null,
    val language: String? = null,
    val coverUrl: String? = null
)
