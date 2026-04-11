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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.ArgumentMatchers
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class GenerateChapterTimingsUseCaseTest {

    private fun anyUuid(): UUID = ArgumentMatchers.any(UUID::class.java) ?: UUID(0L, 0L)

    @Mock
    private lateinit var chapterRepository: ChapterRepository

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
            speechToTextPort = speechToTextPort,
            storagePort = storagePort,
            objectMapper = jacksonObjectMapper()
        )
    }

    @Test
    fun `execute generates timings json and saves it to s3`() {
        val now = LocalDateTime.now()
        val chapter = Chapter.restore(
            id = ChapterId(chapterId),
            bookId = BookId(bookId),
            index = ChapterIndex(1),
            title = "Intro",
            text = "text",
            audioUrl = "https://example.com/audio.mp3",
            timingUrl = null,
            createdAt = now,
            updatedAt = now,
            durationMs = null
        )

        `when`(chapterRepository.findById(ChapterId(chapterId))).thenReturn(Optional.of(chapter))
        `when`(speechToTextPort.recognizeWords("https://example.com/audio.mp3")).thenReturn(
            listOf(
                WordTiming("Hello", 120, 300),
                WordTiming("world", 310, 550)
            )
        )
        var uploadedJson: String? = null
        `when`(storagePort.uploadTimings(anyUuid(), anyUuid(), anyString())).thenAnswer {
            uploadedJson = it.getArgument(2)
            "books/$bookId/$chapterId/timings.json"
        }

        val result = useCase.execute(GenerateChapterTimingsCommand(chapterId.toString(), null))

        assertEquals(chapterId.toString(), result.chapterId)
        assertEquals("books/$bookId/$chapterId/timings.json", result.timingUrl)
        assertEquals(2, result.wordCount)
        assertEquals(550, result.durationMs)

        verify(storagePort, times(1)).uploadTimings(anyUuid(), anyUuid(), anyString())
        val json = uploadedJson ?: error("uploadTimings payload was not captured")
        assertTrue(json.contains("\"word\":\"Hello\""))
        assertTrue(json.contains("\"startMs\":120"))
        assertTrue(json.contains("\"endMs\":550"))
    }

    @Test
    fun `execute throws when chapter and command do not contain audio url`() {
        val now = LocalDateTime.now()
        val chapter = Chapter.restore(
            id = ChapterId(chapterId),
            bookId = BookId(bookId),
            index = ChapterIndex(1),
            title = "Intro",
            text = "text",
            audioUrl = null,
            timingUrl = null,
            createdAt = now,
            updatedAt = now,
            durationMs = null
        )

        `when`(chapterRepository.findById(ChapterId(chapterId))).thenReturn(Optional.of(chapter))

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(GenerateChapterTimingsCommand(chapterId.toString(), null))
        }
    }
}
