package com.yourapp.application.usecase.chapter

import com.audiobook.domain.model.*
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterIndex
import com.yourapp.domain.repository.ChapterRepository
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
        // Проверка уникальности индекса
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
            // Для аудио нужно создать сначала пустую главу, потом добавить аудио
            val emptyChapter = Chapter.createWithText(
                bookId = bookId,
                index = index,
                title = title,
                text = "Temporary text" // Заглушка
            )
            emptyChapter.addAudio(audioUrl, timingUrl)
        } else {
            throw IllegalArgumentException("Chapter must have either text or audio")
        }

        return chapterRepository.save(chapter)
    }
}