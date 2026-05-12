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

    private fun normalizeMediaUrl(value: String?): String? {
        val normalized = value?.trim() ?: return null
        if (normalized.isEmpty()) return null
        if (normalized.equals("string", ignoreCase = true)) return null
        if (normalized.equals("null", ignoreCase = true)) return null
        if (normalized.equals("undefined", ignoreCase = true)) return null
        return normalized
    }

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

        val normalizedAudioUrl = normalizeMediaUrl(input.audioUrl)
        if (normalizedAudioUrl != null) {
            chapter = chapter.addAudio(normalizedAudioUrl, null)
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
    val audioUrl: String? = null
)