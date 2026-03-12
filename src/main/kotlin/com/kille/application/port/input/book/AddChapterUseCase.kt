package com.kille.application.port.input.book

import com.kille.presentation.dto.response.ChapterResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterIndex
import com.kille.domain.repository.BookRepository
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

        var chapter = Chapter.createWithText(
            bookId = BookId(input.bookId),
            index = ChapterIndex(input.index),
            title = input.title,
            text = input.content
        )

        if (input.durationMs != null) {
            chapter = chapter.updateDuration(input.durationMs)
        }

        if (input.audioUrl != null) {
            chapter = chapter.addAudio(input.audioUrl, null)
        }

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
    val audioUrl: String? = null,
    val durationMs: Long? = null
)