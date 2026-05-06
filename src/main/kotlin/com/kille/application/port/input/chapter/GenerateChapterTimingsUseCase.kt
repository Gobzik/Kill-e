package com.kille.application.port.input.chapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.application.port.output.WordTiming
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
import org.springframework.stereotype.Service

data class GenerateChapterTimingsCommand(
    val chapterId: String
)

data class GenerateChapterTimingsResult(
    val chapterId: String,
    val wordCount: Int,
    val durationMs: Long?,
    val timingUrl: String?,
    val textUrl: String?
)

@Service
class GenerateChapterTimingsUseCase(
    private val chapterRepository: ChapterRepository,
    private val speechToTextPort: SpeechToTextPort,
    private val storagePort: StoragePort,
    private val objectMapper: ObjectMapper
) {

    fun execute(command: GenerateChapterTimingsCommand): GenerateChapterTimingsResult {
        val chapterId = ChapterId.fromString(command.chapterId)
        val chapter = loadChapter(chapterId)
        val audioKey = chapter.audioUrl ?: throw IllegalArgumentException("Audio URL is required to generate timings")
        val audioUrl = storagePort.getPresignedUrl(audioKey)

        // Generate timings via STT
        val words = speechToTextPort.recognizeWords(audioUrl)
        val timingsJson = objectMapper.writeValueAsString(TimingsPayload(words))
        val duration = words.maxOfOrNull { it.endMs }

        // Generate Text
        val generatedText = words.joinToString(" ") { it.word }

        // Save timings and text to S3
        val timingKey = storagePort.uploadTimings(chapter.bookId.value, chapter.id.value, timingsJson)

        // Save text as a file in S3 instead of just DB if needed, or save to DB.
        // We'll upload text to storage
        val textKey = storagePort.uploadText(chapter.bookId.value, chapter.id.value, generatedText)

        // Update Chapter
        val updatedChapter = chapter.addAudio(audioKey, timingKey).updateText(textKey)
        chapterRepository.save(updatedChapter)

        return GenerateChapterTimingsResult(
            chapterId = chapterId.value.toString(),
            wordCount = words.size,
            durationMs = duration,
            timingUrl = timingKey,
            textUrl = textKey
        )
    }

    private fun loadChapter(chapterId: ChapterId) =
        chapterRepository.findById(chapterId)
            .orElseThrow { IllegalArgumentException("Chapter with ID ${chapterId.value} not found") }

    private data class TimingsPayload(
        val words: List<WordTiming>
    )
}
