package com.yourapp.application.usecase.chapter

import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.ChapterId
import com.yourapp.domain.repository.ChapterRepository
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
