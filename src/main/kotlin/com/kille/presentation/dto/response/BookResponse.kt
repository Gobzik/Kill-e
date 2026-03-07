package com.kille.presentation.dto.response

import java.util.UUID

data class BookResponse(

    val id: UUID,
    val title: String,
    val author: String,
    val language: String,
    val coverUrl: String?,
    val hasAudio: Boolean,
    val hasText: Boolean,
    val chapterCount: Int,
    val chapters: List<ChapterResponse>
)