package com.kille.infrastructure.persistence.repository

import com.kille.domain.model.AudioProcessing
import com.kille.domain.repository.AudioProcessingRepository
import com.kille.infrastructure.persistence.entity.AudioProcessingEntity
import com.kille.presentation.mapper.AudioProcessingMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AudioProcessingRepositoryImpl(
    private val jpaRepository: AudioProcessingJpaRepository,
    private val mapper: AudioProcessingMapper
) : AudioProcessingRepository {

    override fun save(audioProcessing: AudioProcessing): AudioProcessing {
        val entity = mapper.toEntity(audioProcessing)
        return mapper.toDomain(jpaRepository.save(entity))
    }

    override fun findById(id: UUID): AudioProcessing? {
        return jpaRepository.findById(id).map { mapper.toDomain(it) }.orElse(null)
    }

    override fun findAll(): List<AudioProcessing> {
        return jpaRepository.findAll().map { mapper.toDomain(it) }
    }
}

interface AudioProcessingJpaRepository : JpaRepository<AudioProcessingEntity, UUID>