package com.kille.application.port.input.chapter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.application.port.output.WordTiming
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterIndex
import com.kille.domain.repository.ChapterRepository
import com.kille.domain.repository.ChapterTimingsReviewRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class GenerateChapterTimingsUseCaseTest {

    @Mock
    private lateinit var chapterRepository: ChapterRepository

    @Mock
    private lateinit var reviewRepository: ChapterTimingsReviewRepository

    @Mock
    private lateinit var speechToTextPort: SpeechToTextPort

    @Mock
    private lateinit var storagePort: StoragePort

    private lateinit var useCase: GenerateChapterTimingsUseCase

    private val bookId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val chapterId = UUID.fromString("00000000-0000-0000-0000-000000000002")

    @BeforeEach
    fun setUp() {
        useCase = GenerateChapterTimingsUseCase(
            chapterRepository = chapterRepository,
            reviewRepository = reviewRepository,
            speechToTextPort = speechToTextPort,
            storagePort = storagePort,
            objectMapper = jacksonObjectMapper()
        )
    }

    @Test
    fun `execute starts review and returns first conflict`() {
        val now = LocalDateTime.now()
        val chapter = Chapter.restore(
            id = ChapterId(chapterId),
            bookId = BookId(bookId),
            index = ChapterIndex(1),
            title = "Intro",
            text = "books/$bookId/$chapterId/text.txt",
            audioUrl = "books/$bookId/$chapterId/audio.mp3",
            timingUrl = null,
            createdAt = now,
            updatedAt = now,
            durationMs = null
        )

        `when`(chapterRepository.findById(ChapterId(chapterId))).thenReturn(Optional.of(chapter))
        `when`(storagePort.getPresignedUrl(chapter.audioUrl!!)).thenReturn("https://example.com/audio.mp3")
        `when`(speechToTextPort.recognizeWords("https://example.com/audio.mp3")).thenReturn(
            listOf(
                WordTiming("Hello", 120, 300),
                WordTiming("world", 310, 550)
            )
        )
        `when`(storagePort.getText(bookId, chapterId)).thenReturn("hello planet")
        `when`(reviewRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = useCase.execute(GenerateChapterTimingsCommand(chapterId.toString()))

        assertEquals(chapterId.toString(), result.chapterId)
        assertEquals(2, result.wordCount)
        assertEquals(550, result.durationMs)
        assertTrue(result.remainingConflicts >= 1)
        assertNotNull(result.currentConflict)
        assertFalse(result.canFinalize)
        verify(reviewRepository, times(1)).save(any())
    }

    @Test
    fun `getStatus reflects current text changes and allows finalization when aligned`() {
        val now = LocalDateTime.now()
        val chapter = Chapter.restore(
            id = ChapterId(chapterId),
            bookId = BookId(bookId),
            index = ChapterIndex(1),
            title = "Intro",
            text = "books/$bookId/$chapterId/text.txt",
            audioUrl = "books/$bookId/$chapterId/audio.mp3",
            timingUrl = null,
            createdAt = now,
            updatedAt = now,
            durationMs = null
        )

        val review = com.kille.domain.model.ChapterTimingsReview.create(
            chapterId = ChapterId(chapterId),
            bookId = BookId(bookId),
            audioS3Key = chapter.audioUrl!!,
            timingsJson = "{\"words\":[{\"word\":\"hello\",\"startMs\":120,\"endMs\":300}]}",
            wordCount = 1,
            durationMs = 300
        )

        `when`(chapterRepository.findById(ChapterId(chapterId))).thenReturn(Optional.of(chapter))
        `when`(reviewRepository.findByChapterId(ChapterId(chapterId))).thenReturn(review)
        `when`(storagePort.getText(bookId, chapterId)).thenReturn("hello")

        val result = useCase.getStatus(chapterId.toString())

        assertEquals(0, result.remainingConflicts)
        assertTrue(result.canFinalize)
        assertEquals(1, result.wordCount)
    }
}
