package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.ChapterTimingsReviewJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChapterTimingsReviewJpaRepository : JpaRepository<ChapterTimingsReviewJpaEntity, UUID>

