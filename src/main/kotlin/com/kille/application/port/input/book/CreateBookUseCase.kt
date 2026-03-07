package com.kille.application.port.input.book

import com.kille.presentation.dto.request.CreateBookRequest
import com.kille.presentation.dto.response.BookResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.Book
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterIndex
import com.kille.domain.repository.BookRepository
import com.kille.presentation.mapper.BookMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateBookUseCase(
    private val repository: BookRepository,
    private val mapper: BookMapper
) : UseCase<CreateBookRequest, BookResponse> {

    @Transactional
    override fun execute(input: CreateBookRequest): BookResponse {
        val bookId = BookId.generate()

        val chapters = input.chapters.map { chapterRequest ->
            val chapter = Chapter.createWithText(
                bookId = bookId,
                index = ChapterIndex(chapterRequest.index),
                title = chapterRequest.title,
                text = chapterRequest.text ?: ""
            )
            if (chapterRequest.durationMs != null) {
                chapter.updateDuration(chapterRequest.durationMs)
            }
            chapter
        }

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

        val savedBook = repository.save(book)

        return mapper.toResponse(savedBook)
    }
}