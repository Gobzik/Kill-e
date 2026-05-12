package com.kille.domain.repository

import com.kille.domain.model.AudioProcessing
import java.util.UUID

interface AudioProcessingRepository {
    fun save(audioProcessing: AudioProcessing): AudioProcessing
    fun findById(id: UUID): AudioProcessing?
    fun findAll(): List<AudioProcessing>
}