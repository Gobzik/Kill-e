package com.kille.presentation.dto.response

import com.kille.domain.model.ProcessingStatus
import java.util.UUID

data class AudioProcessingResponse(
    val id: UUID,
    val audioUrl: String,
    val timingsUrl: String?,
    val status: ProcessingStatus,
    val wordCount: Int?,
    val durationMs: Long?
)