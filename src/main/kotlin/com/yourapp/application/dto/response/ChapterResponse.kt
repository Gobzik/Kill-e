package com.yourapp.application.dto.response

import com.yourapp.domain.model.Chapter
import java.time.LocalDateTime
import java.util.UUID

data class ChapterResponse(
    val id: UUID,
    val bookId: UUID,
    val index: Int,
    val title: String?,
    val hasText: Boolean,
    val hasAudio: Boolean,
    val hasTimings: Boolean,
    val isPlayable: Boolean,
    val durationAvailable: Boolean,
    val durationMs: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(chapter: Chapter): ChapterResponse {
            return ChapterResponse(
                id = chapter.id.value,
                bookId = chapter.bookId.value,
                index = chapter.index.value,
                title = chapter.title,
                hasText = chapter.hasText(),
                hasAudio = chapter.hasAudio(),
                hasTimings = chapter.hasTimings(),
                isPlayable = chapter.isPlayable(),
                durationAvailable = chapter.durationAvailable(),
                durationMs = chapter.durationMs,
                createdAt = chapter.createdAt,
                updatedAt = chapter.updatedAt
            )
        }
    }
}
