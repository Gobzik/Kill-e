package com.kille.application.port.input.chapter

import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterIndex
import com.kille.domain.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateChapterUseCase(
    private val chapterRepository: ChapterRepository
) {

    private fun normalizeMediaUrl(value: String?): String? {
        val normalized = value?.trim() ?: return null
        if (normalized.isEmpty()) return null
        if (normalized.equals("string", ignoreCase = true)) return null
        if (normalized.equals("null", ignoreCase = true)) return null
        if (normalized.equals("undefined", ignoreCase = true)) return null
        return normalized
    }

    fun execute(
        bookId: BookId,
        index: ChapterIndex,
        title: String?,
        text: String,
        audioUrl: String?,
        timingUrl: String?
    ): Chapter {
        if (chapterRepository.existsByBookIdAndIndex(bookId, index.value)) {
            throw IllegalArgumentException("Chapter with index $index already exists in book $bookId")
        }

        val chapterText = text.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Chapter text S3 key is required")

        var chapter = Chapter.createWithText(
            bookId = bookId,
            index = index,
            title = title,
            text = chapterText
        )

        val normalizedAudioUrl = normalizeMediaUrl(audioUrl)
        val normalizedTimingUrl = normalizeMediaUrl(timingUrl)

        if (normalizedAudioUrl != null) {
            chapter = chapter.addAudio(normalizedAudioUrl, normalizedTimingUrl)
        }

        return chapterRepository.save(chapter)
    }
}