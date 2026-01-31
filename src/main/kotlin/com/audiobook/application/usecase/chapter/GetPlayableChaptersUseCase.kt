package com.audiobook.application.usecase.chapter

import com.audiobook.domain.model.BookId
import com.audiobook.domain.model.Chapter
import com.audiobook.domain.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetPlayableChaptersUseCase(
    private val chapterRepository: ChapterRepository
) {

    fun execute(bookId: BookId): List<Chapter> {
        return chapterRepository.findByBookIdOrderByIndex(bookId)
            .filter { it.isPlayable() }
    }
}