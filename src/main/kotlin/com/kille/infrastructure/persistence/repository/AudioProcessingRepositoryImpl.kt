package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.AudioProcessingJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AudioProcessingJpaRepository : JpaRepository<AudioProcessingJpaEntity, UUID>
