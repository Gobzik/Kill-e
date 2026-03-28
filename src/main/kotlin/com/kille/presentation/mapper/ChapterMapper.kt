package com.kille.presentation.mapper

import com.kille.presentation.dto.request.CreateChapterRequest
import com.kille.presentation.dto.response.ChapterResponse
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterIndex
import java.time.LocalDateTime
import java.util.*

object ChapterMapper {

    fun toResponse(chapter: Chapter): ChapterResponse {
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

    fun toDomain(
        request: CreateChapterRequest,
        id: UUID = UUID.randomUUID(),
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): Chapter {
        return Chapter.restore(
            id = ChapterId.fromString(id.toString()),
            bookId = BookId.fromString(request.bookId),
            index = ChapterIndex(request.index),
            title = request.title,
            text = request.text,
            audioUrl = request.audioUrl,
            timingUrl = request.timingUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
            durationMs = request.durationMs
        )
    }
}