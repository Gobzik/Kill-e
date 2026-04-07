package com.kille.application.port.input.chapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.application.port.output.WordTiming
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
import org.springframework.stereotype.Service

data class GenerateChapterTimingsCommand(
    val chapterId: String,
    val audioUrl: String? = null
)

data class GenerateChapterTimingsResult(
    val chapterId: String,
    val timingUrl: String,
    val wordCount: Int,
    val durationMs: Long?
)

@Service
class GenerateChapterTimingsUseCase(
    private val chapterRepository: ChapterRepository,
    private val speechToTextPort: SpeechToTextPort,
    private val storagePort: StoragePort,
    private val objectMapper: ObjectMapper
) {

    fun execute(command: GenerateChapterTimingsCommand): GenerateChapterTimingsResult {
        val chapter = chapterRepository.findById(ChapterId.fromString(command.chapterId))
            .orElseThrow { RuntimeException("Chapter with ID ${command.chapterId} not found") }

        val sourceAudioUrl = command.audioUrl ?: chapter.audioUrl
        if (sourceAudioUrl.isNullOrBlank()) {
            throw IllegalArgumentException("Audio URL is required to generate timings")
        }

        val words = speechToTextPort.recognizeWords(sourceAudioUrl)
        val timingsJson = objectMapper.writeValueAsString(TimingsPayload(words))

        val timingKey = storagePort.uploadTimings(chapter.bookId.value, chapter.id.value, timingsJson)

        var updatedChapter = chapter.addAudio(sourceAudioUrl, timingKey)
        val duration = words.maxOfOrNull { it.endMs }
        if (duration != null && duration > 0) {
            updatedChapter = updatedChapter.updateDuration(duration)
        }

        chapterRepository.save(updatedChapter)

        return GenerateChapterTimingsResult(
            chapterId = command.chapterId,
            timingUrl = timingKey,
            wordCount = words.size,
            durationMs = duration
        )
    }

    private data class TimingsPayload(
        val words: List<WordTiming>
    )
}

