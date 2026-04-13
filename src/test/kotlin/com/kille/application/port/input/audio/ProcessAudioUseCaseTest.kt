package com.kille.application.port.input.audio

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.application.port.output.WordTiming
import com.kille.domain.model.AudioProcessing
import com.kille.domain.model.ProcessingStatus
import com.kille.domain.repository.AudioProcessingRepository
import com.kille.presentation.dto.response.AudioProcessingResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ProcessAudioUseCaseTest {

    @Mock
    private lateinit var audioProcessingRepository: AudioProcessingRepository

    @Mock
    private lateinit var speechToTextPort: SpeechToTextPort

    @Mock
    private lateinit var storagePort: StoragePort

    private lateinit var useCase: ProcessAudioUseCase

    private val id = UUID.fromString("00000000-0000-0000-0000-000000000001")

    @BeforeEach
    fun setUp() {
        useCase = ProcessAudioUseCase(
            audioProcessingRepository = audioProcessingRepository,
            speechToTextPort = speechToTextPort,
            storagePort = storagePort,
            objectMapper = jacksonObjectMapper()
        )
    }

    @Test
    fun `initiateProcessing uploads file and saves PENDING record`() {
        val file = MockMultipartFile("audio", "test.mp3", "audio/mpeg", ByteArray(10))
        val expectedKey = "audio-processing/${UUID.randomUUID()}/audio.mp3"
        `when`(storagePort.uploadFile(anyString(), eq(file))).thenReturn(expectedKey)
        `when`(audioProcessingRepository.save(any(AudioProcessing::class.java))).thenAnswer { it.arguments[0] }

        val result = useCase.initiateProcessing(ProcessAudioCommand(file))

        assertEquals(ProcessingStatus.PENDING, result.status)
        assertNull(result.timingsKey)
        assertNotNull(result.audioKey)
        verify(storagePort).uploadFile(anyString(), eq(file))
        verify(audioProcessingRepository).save(any(AudioProcessing::class.java))
    }

    @Test
    fun `processTimings transitions to PROCESSING then COMPLETED on success`() {
        val audioKey = "audio-processing/$id/audio.mp3"
        val presignedUrl = "https://s3.example.com/$audioKey?presigned=true"
        val timingsKey = "audio-processing/$id/timings.json"
        val words = listOf(WordTiming("Hello", 0, 300), WordTiming("world", 310, 600))

        val existing = AudioProcessing(
            id = id,
            audioS3Key = audioKey,
            status = ProcessingStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        `when`(audioProcessingRepository.findById(id)).thenReturn(existing)
        `when`(audioProcessingRepository.save(any(AudioProcessing::class.java))).thenAnswer { it.arguments[0] }
        `when`(storagePort.getPresignedUrl(audioKey)).thenReturn(presignedUrl)
        `when`(speechToTextPort.recognizeWords(presignedUrl)).thenReturn(words)
        `when`(storagePort.uploadContent(anyString(), anyString(), anyString())).thenReturn(timingsKey)

        useCase.processTimings(id)

        val captor = org.mockito.ArgumentCaptor.forClass(AudioProcessing::class.java)
        verify(audioProcessingRepository, times(2)).save(captor.capture())

        val firstSave = captor.allValues[0]
        assertEquals(ProcessingStatus.PROCESSING, firstSave.status)

        val secondSave = captor.allValues[1]
        assertEquals(ProcessingStatus.COMPLETED, secondSave.status)
        assertEquals(timingsKey, secondSave.timingsS3Key)
        assertEquals(2, secondSave.wordCount)
        assertEquals(600L, secondSave.durationMs)
    }

    @Test
    fun `processTimings transitions to FAILED on error`() {
        val audioKey = "audio-processing/$id/audio.mp3"
        val presignedUrl = "https://s3.example.com/presigned"

        val existing = AudioProcessing(
            id = id,
            audioS3Key = audioKey,
            status = ProcessingStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        `when`(audioProcessingRepository.findById(id)).thenReturn(existing)
        `when`(audioProcessingRepository.save(any(AudioProcessing::class.java))).thenAnswer { it.arguments[0] }
        `when`(storagePort.getPresignedUrl(audioKey)).thenReturn(presignedUrl)
        `when`(speechToTextPort.recognizeWords(presignedUrl)).thenThrow(RuntimeException("STT failed"))

        assertThrows<RuntimeException> { useCase.processTimings(id) }

        val captor = org.mockito.ArgumentCaptor.forClass(AudioProcessing::class.java)
        verify(audioProcessingRepository, times(2)).save(captor.capture())

        val failedSave = captor.allValues[1]
        assertEquals(ProcessingStatus.FAILED, failedSave.status)
        assertEquals("STT failed", failedSave.errorMessage)
    }

    @Test
    fun `processTimings throws when record not found`() {
        `when`(audioProcessingRepository.findById(id)).thenReturn(null)

        assertThrows<IllegalArgumentException> { useCase.processTimings(id) }
    }

    @Test
    fun `getStatus returns response for existing record`() {
        val audioKey = "audio-processing/$id/audio.mp3"
        val timingsKey = "audio-processing/$id/timings.json"

        val existing = AudioProcessing(
            id = id,
            audioS3Key = audioKey,
            timingsS3Key = timingsKey,
            status = ProcessingStatus.COMPLETED,
            wordCount = 5,
            durationMs = 1200,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        `when`(audioProcessingRepository.findById(id)).thenReturn(existing)

        val result = useCase.getStatus(id)

        assertEquals(id, result.id)
        assertEquals(audioKey, result.audioKey)
        assertEquals(timingsKey, result.timingsKey)
        assertEquals(ProcessingStatus.COMPLETED, result.status)
        assertEquals(5, result.wordCount)
        assertEquals(1200L, result.durationMs)
    }

    @Test
    fun `getStatus throws when record not found`() {
        `when`(audioProcessingRepository.findById(id)).thenReturn(null)

        assertThrows<IllegalArgumentException> { useCase.getStatus(id) }
    }
}
