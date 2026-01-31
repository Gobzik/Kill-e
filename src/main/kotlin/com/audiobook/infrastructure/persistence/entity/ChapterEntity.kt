package com.audiobook.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "chapters")
class ChapterEntity(
    @Id
    val id: UUID,

    @Column(name = "book_id", nullable = false)
    val bookId: UUID,

    @Column(name = "chapter_index", nullable = false)
    val index: Int,

    @Column(name = "title", length = 500)
    val title: String?,

    @Lob
    @Column(name = "text", columnDefinition = "TEXT")
    val text: String?,

    @Column(name = "audio_url", length = 2000)
    val audioUrl: String?,

    @Column(name = "timing_url", length = 2000)
    val timingUrl: String?,

    @Column(name = "duration_ms")
    val durationMs: Long?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime
)