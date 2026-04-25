package com.kille.infrastructure.persistence.entity

import com.kille.domain.model.ChapterTimingsReviewStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "chapter_timings_reviews")
class ChapterTimingsReviewJpaEntity(
    @Id
    var chapterId: UUID,

    @Column(name = "book_id", nullable = false)
    var bookId: UUID,

    @Column(name = "audio_s3_key", nullable = false)
    var audioS3Key: String,

    @Column(name = "timings_json", columnDefinition = "TEXT", nullable = false)
    var timingsJson: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ChapterTimingsReviewStatus,

    @Column(name = "word_count", nullable = false)
    var wordCount: Int,

    @Column(name = "duration_ms")
    var durationMs: Long? = null,

    @Column(name = "timing_url", length = 2000)
    var timingUrl: String? = null,

    @Column(name = "error_message", length = 1000)
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime
)

