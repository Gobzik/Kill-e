package com.kille.domain.model

import java.time.Instant
import java.util.UUID

data class AudioProcessing(
    val id: UUID,
    val audioS3Key: String,
    val timingsS3Key: String? = null,
    val status: ProcessingStatus,
    val durationMs: Long? = null,
    val wordCount: Int? = null,
    val errorMessage: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun complete(timingsS3Key: String, wordCount: Int, durationMs: Long?) = copy(
        timingsS3Key = timingsS3Key,
        wordCount = wordCount,
        durationMs = durationMs,
        status = ProcessingStatus.COMPLETED,
        updatedAt = Instant.now()
    )

    fun fail(errorMessage: String? = null) = copy(
        status = ProcessingStatus.FAILED,
        errorMessage = errorMessage,
        updatedAt = Instant.now()
    )
}

enum class ProcessingStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
