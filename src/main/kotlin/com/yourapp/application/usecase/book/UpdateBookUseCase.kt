package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.repository.BookRepository
import com.yourapp.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateBookUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCase<UpdateBookCommand, BookResponse> {

    @Transactional
    override fun execute(input: UpdateBookCommand): BookResponse {
        val book = repository.findById(BookId(input.bookId))
            ?: throw RuntimeException("Book with ID ${input.bookId} not found")

        val updatedBook = com.yourapp.domain.model.Book.restore(
            id = book.id,
            title = input.title ?: book.title,
            author = input.author ?: book.author,
            language = input.language ?: book.language,
            coverUrl = input.coverUrl ?: book.coverUrl,
            chapters = book.chapters(),
            audio = book.audio,
            text = book.text
        )
        val savedBook = repository.save(updatedBook)
        return mapper.toResponse(savedBook)
    }
}

data class UpdateBookCommand(
    val bookId: java.util.UUID,
    val title: String? = null,
    val author: String? = null,
    val language: String? = null,
    val coverUrl: String? = null
)
