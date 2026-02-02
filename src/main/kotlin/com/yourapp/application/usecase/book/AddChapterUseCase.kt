package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AddChapterUseCase(
    private val repository: BookRepository
) : UseCase<AddChapterCommand, ChapterResponse> {

    @Transactional
    override fun execute(input: AddChapterCommand): ChapterResponse {
        val book = repository.findById(BookId(input.bookId))
            ?: throw RuntimeException("Book with ID ${input.bookId} not found")

        val chapter = Chapter.createWithText(
            bookId = BookId(input.bookId),
            index = com.yourapp.domain.model.ChapterIndex(input.index),
            title = input.title,
            text = input.content
        )

        book.addChapter(chapter)
        repository.save(book)

        return ChapterResponse.fromDomain(chapter)
    }
}

data class AddChapterCommand(
    val bookId: UUID,
    val title: String,
    val content: String,
    val index: Int,
    val audioUrl: String? = null
)