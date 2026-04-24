package com.kille.infrastructure.persistence.entity


import com.kille.domain.model.ProcessingStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_processing")
data class AudioProcessingJpaEntity(
    @Id
    val id: UUID,

    @Column(name = "audio_s3_key", nullable = false)
    val audioS3Key: String,

    @Column(name = "timings_s3_key")
    val timingsS3Key: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ProcessingStatus,

    @Column(name = "duration_ms")
    val durationMs: Long? = null,

    @Column(name = "word_count")
    val wordCount: Int? = null,

    @Column(name = "error_message", length = 1000)
    val errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)