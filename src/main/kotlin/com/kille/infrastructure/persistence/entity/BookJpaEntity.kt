package com.kille.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "books")
class BookEntityJpa(

    @Id
    @Column(columnDefinition = "UUID")
    var id: UUID,

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
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        mappedBy = "book",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("index ASC")
    var chapters: MutableList<ChapterJpaEntity> = mutableListOf()
)