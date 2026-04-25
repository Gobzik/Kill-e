package com.kille.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "chapters")
class ChapterJpaEntity(
    @Id
    var id: UUID,

    @Column(name = "book_id", nullable = false)
    var bookId: UUID,

    @Column(name = "chapter_index", nullable = false)
    var index: Int,

    @Column(name = "title", length = 500)
    var title: String?,

    @Lob
    @Column(name = "text", columnDefinition = "TEXT")
    var text: String?,

    @Column(name = "audio_url", length = 2000)
    var audioUrl: String?,

    @Column(name = "timing_url", length = 2000)
    var timingUrl: String?,

    @Column(name = "duration_ms")
    var durationMs: Long?,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", insertable = false, updatable = false)
    var book: BookEntityJpa? = null
}
