package com.yourapp.application.usecase.chapter

import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.ChapterId
import com.yourapp.domain.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetChapterUseCase(
    private val repository: ChapterRepository
) : UseCase<String, ChapterResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: String): ChapterResponse {
        val chapter = repository.findById(ChapterId.fromString(input))
            .orElseThrow { RuntimeException("Chapter with ID $input not found") }

        return ChapterResponse.fromDomain(chapter)
    }
}
