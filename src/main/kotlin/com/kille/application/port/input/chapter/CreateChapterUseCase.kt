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

    fun execute(
        bookId: BookId,
        index: ChapterIndex,
        title: String?,
        text: String?,
        audioUrl: String?,
        timingUrl: String?
    ): Chapter {
        if (chapterRepository.existsByBookIdAndIndex(bookId, index.value)) {
            throw IllegalArgumentException("Chapter with index $index already exists in book $bookId")
        }

        val chapter = if (text != null) {
            Chapter.createWithText(
                bookId = bookId,
                index = index,
                title = title,
                text = text
            )
        } else if (audioUrl != null) {
            val emptyChapter = Chapter.createWithText(
                bookId = bookId,
                index = index,
                title = title,
                text = "Temporary text"
            )
            emptyChapter.addAudio(audioUrl, timingUrl)
        } else {
            throw IllegalArgumentException("Chapter must have either text or audio")
        }

        return chapterRepository.save(chapter)
    }
}