package com.kille.application.port.input.chapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.application.port.output.WordTiming
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterTimingsReview
import com.kille.domain.model.ChapterTimingsReviewStatus
import com.kille.domain.repository.ChapterRepository
import com.kille.domain.repository.ChapterTimingsReviewRepository
import org.springframework.stereotype.Service

data class GenerateChapterTimingsCommand(
    val chapterId: String
)

data class ChapterTimingConflict(
    val index: Int,
    val textWord: String?,
    val recognizedWord: String,
    val startMs: Long,
    val endMs: Long,
    val suggestion: String
)

data class GenerateChapterTimingsResult(
    val chapterId: String,
    val reviewStatus: ChapterTimingsReviewStatus,
    val wordCount: Int,
    val durationMs: Long?,
    val currentConflict: ChapterTimingConflict?,
    val remainingConflicts: Int,
    val canFinalize: Boolean
)

data class FinalizeChapterTimingsResult(
    val chapterId: String,
    val timingUrl: String,
    val wordCount: Int,
    val durationMs: Long?
)

@Service
class GenerateChapterTimingsUseCase(
    private val chapterRepository: ChapterRepository,
    private val reviewRepository: ChapterTimingsReviewRepository,
    private val speechToTextPort: SpeechToTextPort,
    private val storagePort: StoragePort,
    private val objectMapper: ObjectMapper
) {

    fun execute(command: GenerateChapterTimingsCommand): GenerateChapterTimingsResult {
        val chapterId = ChapterId.fromString(command.chapterId)
        val chapter = loadChapter(chapterId)
        val audioKey = chapter.audioUrl ?: throw IllegalArgumentException("Audio URL is required to generate timings")
        val audioUrl = storagePort.getPresignedUrl(audioKey)

        // If timings already exist in storage for this chapter, use them instead of calling external SpeechKit
        val existingTimings = storagePort.getTimings(chapter.bookId.value, chapter.id.value)
        val (words, timingsJson, duration) = if (!existingTimings.isNullOrBlank()) {
            val existingWords = decodeWords(existingTimings)
            Triple(existingWords, existingTimings, existingWords.maxOfOrNull { it.endMs })
        } else {
            val recognized = speechToTextPort.recognizeWords(audioUrl)
            val json = objectMapper.writeValueAsString(TimingsPayload(recognized))
            Triple(recognized, json, recognized.maxOfOrNull { it.endMs })
        }

        val review = reviewRepository.save(
            ChapterTimingsReview.create(
                chapterId = chapter.id,
                bookId = chapter.bookId,
                audioS3Key = audioKey,
                timingsJson = timingsJson,
                wordCount = words.size,
                durationMs = duration
            ).markReviewing()
        )

        return buildResult(review, chapter.id, currentText = storagePort.getText(chapter.bookId.value, chapter.id.value), words = words)
    }

    fun getStatus(chapterId: String): GenerateChapterTimingsResult {
        val chapterIdValue = ChapterId.fromString(chapterId)
        val review = reviewRepository.findByChapterId(chapterIdValue)
            ?: throw IllegalArgumentException("Chapter timing review for $chapterId not found. Start processing first.")
        val chapter = loadChapter(review.chapterId)
        val words = decodeWords(review.timingsJson)
        return buildResult(review = review, chapterId = chapter.id, currentText = storagePort.getText(chapter.bookId.value, chapter.id.value), words = words)
    }

    fun finalize(chapterId: String): FinalizeChapterTimingsResult {
        val chapterIdValue = ChapterId.fromString(chapterId)
        val review = reviewRepository.findByChapterId(chapterIdValue)
            ?: throw IllegalArgumentException("Chapter timing review for $chapterId not found. Start processing first.")
        val chapter = loadChapter(review.chapterId)
        val words = decodeWords(review.timingsJson)
        val conflicts = compareWords(storagePort.getText(chapter.bookId.value, chapter.id.value), words)
        if (conflicts.isNotEmpty()) {
            throw IllegalStateException(
                "There are unresolved conflicts. First conflict: ${conflicts.first().recognizedWord} vs ${conflicts.first().textWord ?: "<missing>"}"
            )
        }

        val timingKey = storagePort.uploadTimings(chapter.bookId.value, chapter.id.value, review.timingsJson)
        val currentAudio = chapter.audioUrl ?: throw IllegalStateException("Chapter has no audio to attach timings")
        chapterRepository.save(chapter.addAudio(currentAudio, timingKey))
        reviewRepository.save(review.complete(timingKey))

        return FinalizeChapterTimingsResult(
            chapterId = chapterId,
            timingUrl = timingKey,
            wordCount = review.wordCount,
            durationMs = review.durationMs
        )
    }

    private fun loadChapter(chapterId: ChapterId) =
        chapterRepository.findById(chapterId)
            .orElseThrow { IllegalArgumentException("Chapter with ID ${chapterId.value} not found") }

    private fun decodeWords(timingsJson: String): List<WordTiming> {
        val payload = objectMapper.readValue(timingsJson, TimingsPayload::class.java)
        return payload.words
    }

    private fun buildResult(
        review: ChapterTimingsReview,
        chapterId: ChapterId,
        currentText: String?,
        words: List<WordTiming>
    ): GenerateChapterTimingsResult {
        val conflicts = compareWords(currentText, words)
        return GenerateChapterTimingsResult(
            chapterId = chapterId.value.toString(),
            reviewStatus = review.status,
            wordCount = review.wordCount,
            durationMs = review.durationMs,
            currentConflict = conflicts.firstOrNull(),
            remainingConflicts = conflicts.size,
            canFinalize = conflicts.isEmpty() && review.status != ChapterTimingsReviewStatus.COMPLETED
        )
    }

    private fun compareWords(textContent: String?, timings: List<WordTiming>): List<ChapterTimingConflict> {
        val textWords = tokenize(textContent.orEmpty())
        val maxSize = maxOf(textWords.size, timings.size)
        val conflicts = mutableListOf<ChapterTimingConflict>()

        for (index in 0 until maxSize) {
            val textWord = textWords.getOrNull(index)
            val timing = timings.getOrNull(index)
            if (textWord == null || timing == null) {
                conflicts += ChapterTimingConflict(
                    index = index,
                    textWord = textWord,
                    recognizedWord = timing?.word ?: "<missing>",
                    startMs = timing?.startMs ?: -1,
                    endMs = timing?.endMs ?: -1,
                    suggestion = timing?.word ?: textWord.orEmpty()
                )
                continue
            }

            if (!textWord.equals(timing.word, ignoreCase = true)) {
                conflicts += ChapterTimingConflict(
                    index = index,
                    textWord = textWord,
                    recognizedWord = timing.word,
                    startMs = timing.startMs,
                    endMs = timing.endMs,
                    suggestion = timing.word
                )
            }
        }

        return conflicts
    }

    private fun tokenize(text: String): List<String> {
        return Regex("[\\p{L}\\p{N}'’-]+")
            .findAll(text)
            .map { it.value.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toList()
    }

    private data class TimingsPayload(
        val words: List<WordTiming>
    )
}

