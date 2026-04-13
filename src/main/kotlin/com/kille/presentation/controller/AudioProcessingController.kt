package com.kille.presentation.controller

import com.kille.application.port.input.audio.ProcessAudioUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import com.kille.application.port.input.audio.ProcessAudioCommand
import com.kille.presentation.dto.response.AudioProcessingResponse
@RestController
@RequestMapping("/api/v1/audio-processing")
class AudioProcessingController(
    private val processAudioUseCase: ProcessAudioUseCase
) {

    @PostMapping("/upload")
    fun uploadAndProcess(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<AudioProcessingResponse> {
        val result = processAudioUseCase.initiateProcessing(
            ProcessAudioCommand(file)
        )
        return ResponseEntity.accepted().body(result)
    }

    @GetMapping("/{id}/status")
    fun getStatus(@PathVariable id: UUID): ResponseEntity<AudioProcessingResponse> {
        val result = processAudioUseCase.getStatus(id)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/process")
    fun triggerProcessing(@PathVariable id: UUID): ResponseEntity<Void> {
        processAudioUseCase.processTimings(id)
        return ResponseEntity.accepted().build()
    }
}