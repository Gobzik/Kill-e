package com.kille.application.port.input.chapter

import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.repository.ChapterRepository
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