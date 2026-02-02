package com.yourapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "books")
data class BookEntityJpa(

    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(nullable = false, length = 100)
    var author: String,

    @Column(nullable = false, length = 50)
    var language: String,

    @Column(name = "cover_url", length = 500)
    var coverUrl: String? = null,

    @Column(name = "has_audio", nullable = false)
    var hasAudio: Boolean,

    @Column(name = "has_text", nullable = false)
    var hasText: Boolean,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        mappedBy = "book",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("index ASC")
    var chapters: MutableList<ChapterEntityJpa> = mutableListOf()
)

@Entity
@Table(name = "chapters")
data class ChapterEntityJpa(

    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    var book: BookEntityJpa,

    @Column(name = "title", length = 500)
    var title: String?,

    @Lob
    @Column(name = "text", columnDefinition = "TEXT")
    var content: String?,

    @Column(name = "chapter_index", nullable = false)
    var index: Int,

    @Column(name = "audio_url", length = 2000)
    var audioUrl: String? = null,

    @Column(name = "timing_url", length = 2000)
    var timingUrl: String? = null,

    @Column(name = "duration_ms")
    var durationMs: Long? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
