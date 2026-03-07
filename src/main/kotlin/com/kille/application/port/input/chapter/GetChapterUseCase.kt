package com.kille.application.port.input.chapter

import com.kille.presentation.dto.response.ChapterResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
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
