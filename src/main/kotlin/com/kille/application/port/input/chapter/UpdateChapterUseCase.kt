package com.kille.application.port.input.chapter

import com.kille.presentation.dto.response.ChapterResponse
import com.kille.application.port.input.UseCase
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateChapterUseCase(
    private val repository: ChapterRepository
) : UseCase<UpdateChapterCommand, ChapterResponse> {

    @Transactional
    override fun execute(input: UpdateChapterCommand): ChapterResponse {
        val chapterId = ChapterId.fromString(input.chapterId)
        val chapter = repository.findById(chapterId)
            .orElseThrow { RuntimeException("Chapter with ID ${input.chapterId} not found") }

        val updatedChapter = when {
            input.text != null -> chapter.updateText(input.text)
            input.audioUrl != null -> chapter.addAudio(input.audioUrl, input.timingUrl)
            else -> chapter
        }

        val saved = repository.save(updatedChapter)
        return ChapterResponse.fromDomain(saved)
    }
}

data class UpdateChapterCommand(
    val chapterId: String,
    val text: String? = null,
    val audioUrl: String? = null,
    val timingUrl: String? = null
)
