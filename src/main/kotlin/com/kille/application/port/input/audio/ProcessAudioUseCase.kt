package com.kille.application.port.input.audio

import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.presentation.dto.response.AudioProcessingResponse
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.StoragePort
import com.kille.domain.model.AudioProcessing
import com.kille.domain.model.ProcessingStatus
import com.kille.domain.repository.AudioProcessingRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.UUID

data class ProcessAudioCommand(
    val audioFile: MultipartFile
)

@Service
class ProcessAudioUseCase(
    private val audioProcessingRepository: AudioProcessingRepository,
    private val speechToTextPort: SpeechToTextPort,
    private val storagePort: StoragePort,
    private val objectMapper: ObjectMapper
) {

    fun initiateProcessing(command: ProcessAudioCommand): AudioProcessingResponse {
        val id = UUID.randomUUID()

        val extension = command.audioFile.originalFilename?.substringAfterLast('.', "mp3") ?: "mp3"
        val audioKey = "audio-processing/$id/audio.$extension"
        val audioS3Key = storagePort.uploadFile(audioKey, command.audioFile)

        val audioProcessing = AudioProcessing(
            id = id,
            audioS3Key = audioS3Key,
            status = ProcessingStatus.PENDING,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        audioProcessingRepository.save(audioProcessing)

        return AudioProcessingResponse(
            id = id,
            audioKey = audioS3Key,
            timingsKey = null,
            status = ProcessingStatus.PENDING,
            wordCount = null,
            durationMs = null
        )
    }

    fun processTimings(id: UUID) {
        val audioProcessing = audioProcessingRepository.findById(id)
            ?: throw IllegalArgumentException("Audio processing with ID $id not found")

        audioProcessingRepository.save(
            audioProcessing.copy(
                status = ProcessingStatus.PROCESSING,
                updatedAt = Instant.now()
            )
        )

        try {
            val audioUrl = storagePort.getPresignedUrl(audioProcessing.audioS3Key)
            val words = speechToTextPort.recognizeWords(audioUrl)
            val timingsJson = objectMapper.writeValueAsString(mapOf("words" to words))

            val timingsKey = "audio-processing/$id/timings.json"
            val timingsS3Key = storagePort.uploadContent(timingsKey, timingsJson, "application/json")

            val duration = words.maxOfOrNull { it.endMs }
            audioProcessingRepository.save(
                audioProcessing.complete(timingsS3Key, words.size, duration)
            )
        } catch (ex: Exception) {
            audioProcessingRepository.save(
                audioProcessing.fail(ex.message)
            )
            throw ex
        }
    }

    fun getStatus(id: UUID): AudioProcessingResponse {
        val audioProcessing = audioProcessingRepository.findById(id)
            ?: throw IllegalArgumentException("Processing with ID $id not found")

        return AudioProcessingResponse(
            id = audioProcessing.id,
            audioKey = audioProcessing.audioS3Key,
            timingsKey = audioProcessing.timingsS3Key,
            status = audioProcessing.status,
            wordCount = audioProcessing.wordCount,
            durationMs = audioProcessing.durationMs
        )
    }
}
