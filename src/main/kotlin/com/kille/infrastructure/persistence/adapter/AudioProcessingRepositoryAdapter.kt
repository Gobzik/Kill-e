package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.AudioProcessing
import com.kille.domain.repository.AudioProcessingRepository
import com.kille.infrastructure.persistence.entity.AudioProcessingJpaEntity
import com.kille.infrastructure.persistence.repository.AudioProcessingJpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AudioProcessingRepositoryAdapter(
    private val jpaRepository: AudioProcessingJpaRepository
) : AudioProcessingRepository {

    override fun save(audioProcessing: AudioProcessing): AudioProcessing {
        val entity = audioProcessing.toEntity()
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: UUID): AudioProcessing? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<AudioProcessing> {
        return jpaRepository.findAll().map { it.toDomain() }
    }
}

fun AudioProcessingJpaEntity.toDomain(): AudioProcessing {
    return AudioProcessing(
        id = id,
        audioS3Key = audioS3Key,
        timingsS3Key = timingsS3Key,
        status = status,
        durationMs = durationMs,
        wordCount = wordCount,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AudioProcessing.toEntity(): AudioProcessingJpaEntity {
    return AudioProcessingJpaEntity(
        id = id,
        audioS3Key = audioS3Key,
        timingsS3Key = timingsS3Key,
        status = status,
        durationMs = durationMs,
        wordCount = wordCount,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
