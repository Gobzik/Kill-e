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

    private fun normalizeMediaUrl(value: String?): String? {
        val normalized = value?.trim() ?: return null
        if (normalized.isEmpty()) return null
        if (normalized.equals("string", ignoreCase = true)) return null
        if (normalized.equals("null", ignoreCase = true)) return null
        if (normalized.equals("undefined", ignoreCase = true)) return null
        return normalized
    }

    @Transactional
    override fun execute(input: UpdateChapterCommand): ChapterResponse {
        val chapterId = ChapterId.fromString(input.chapterId)
        val chapter = repository.findById(chapterId)
            .orElseThrow { RuntimeException("Chapter with ID ${input.chapterId} not found") }

        val normalizedAudioUrl = normalizeMediaUrl(input.audioUrl)
        val normalizedTimingUrl = normalizeMediaUrl(input.timingUrl)

        val updatedChapter = when {
            input.text != null -> chapter.updateText(input.text)
            normalizedAudioUrl != null -> chapter.addAudio(normalizedAudioUrl, normalizedTimingUrl)
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
