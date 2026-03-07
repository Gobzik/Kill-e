package com.kille.application.port.input.chapter

import com.kille.application.port.input.UseCase
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteChapterUseCase(
    private val repository: ChapterRepository
) : UseCase<String, Unit> {

    @Transactional
    override fun execute(input: String) {
        repository.deleteById(ChapterId.fromString(input))
    }
}
