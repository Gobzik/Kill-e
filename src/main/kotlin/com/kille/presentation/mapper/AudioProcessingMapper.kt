package com.kille.presentation.mapper

import com.kille.domain.model.AudioProcessing
import com.kille.infrastructure.persistence.entity.AudioProcessingEntity
import org.springframework.stereotype.Component

@Component
class AudioProcessingMapper {

    fun toEntity(domain: AudioProcessing): AudioProcessingEntity {
        return AudioProcessingEntity(
            id = domain.id,
            audioS3Key = domain.audioS3Key,
            timingsS3Key = domain.timingsS3Key,
            status = domain.status,
            durationMs = domain.durationMs,
            wordCount = domain.wordCount,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    fun toDomain(entity: AudioProcessingEntity): AudioProcessing {
        return AudioProcessing(
            id = entity.id,
            audioS3Key = entity.audioS3Key,
            timingsS3Key = entity.timingsS3Key,
            status = entity.status,
            durationMs = entity.durationMs,
            wordCount = entity.wordCount,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}