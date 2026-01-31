package com.yourapp.application.dto.response

import java.util.UUID

data class ChapterResponse(
    val id: UUID,
    val title: String,
    val index: Int,
    val hasAudio: Boolean,
    val hasText: Boolean
)