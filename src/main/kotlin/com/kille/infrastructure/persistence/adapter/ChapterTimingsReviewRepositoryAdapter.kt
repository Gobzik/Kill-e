package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.BookId
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterTimingsReview
import com.kille.domain.repository.ChapterTimingsReviewRepository
import com.kille.infrastructure.persistence.entity.ChapterTimingsReviewJpaEntity
import com.kille.infrastructure.persistence.repository.ChapterTimingsReviewJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ChapterTimingsReviewRepositoryAdapter(
    private val jpaRepository: ChapterTimingsReviewJpaRepository
) : ChapterTimingsReviewRepository {

    override fun save(review: ChapterTimingsReview): ChapterTimingsReview {
        return jpaRepository.save(review.toEntity()).toDomain()
    }

    override fun findByChapterId(chapterId: ChapterId): ChapterTimingsReview? {
        return jpaRepository.findById(chapterId.value).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<ChapterTimingsReview> {
        return jpaRepository.findAll().map { it.toDomain() }
    }
}

fun ChapterTimingsReviewJpaEntity.toDomain(): ChapterTimingsReview {
    return ChapterTimingsReview(
        chapterId = ChapterId.fromString(chapterId.toString()),
        bookId = BookId.fromString(bookId.toString()),
        audioS3Key = audioS3Key,
        timingsJson = timingsJson,
        status = status,
        wordCount = wordCount,
        durationMs = durationMs,
        timingUrl = timingUrl,
        errorMessage = errorMessage,
        createdAt = createdAt,
        _updatedAt = updatedAt
    )
}

fun ChapterTimingsReview.toEntity(): ChapterTimingsReviewJpaEntity {
    return ChapterTimingsReviewJpaEntity(
        chapterId = chapterId.value,
        bookId = bookId.value,
        audioS3Key = audioS3Key,
        timingsJson = timingsJson,
        status = status,
        wordCount = wordCount,
        durationMs = durationMs,
        timingUrl = timingUrl,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


