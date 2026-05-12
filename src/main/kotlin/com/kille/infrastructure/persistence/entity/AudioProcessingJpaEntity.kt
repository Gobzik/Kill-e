package com.kille.infrastructure.persistence.entity


import com.kille.domain.model.ProcessingStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audio_processing")
class AudioProcessingJpaEntity(
    @Id
    var id: UUID,

    @Column(name = "audio_s3_key", nullable = false)
    var audioS3Key: String,

    @Column(name = "timings_s3_key")
    var timingsS3Key: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProcessingStatus,

    @Column(name = "duration_ms")
    var durationMs: Long? = null,

    @Column(name = "word_count")
    var wordCount: Int? = null,

    @Column(name = "error_message", length = 1000)
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
)