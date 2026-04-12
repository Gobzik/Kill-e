package com.kille.domain.model
import java.util.UUID
data class AudioProcessing(
    val id: UUID,
    val audioS3Key: String,
    val timingsS3Key: String? = null,
    val status: ProcessingStatus,
    val durationMs: Long? = null,
    val wordCount: Int? = null,
    val createdAt: java.time.Instant,
    val updatedAt: java.time.Instant
) {
    fun complete(timingsS3Key: String, wordCount: Int, durationMs: Long?) = copy(
        timingsS3Key = timingsS3Key,
        wordCount = wordCount,
        durationMs = durationMs,
        status = ProcessingStatus.COMPLETED,
        updatedAt = java.time.Instant.now()
    )

    fun fail() = copy(
        status = ProcessingStatus.FAILED,
        updatedAt = java.time.Instant.now()
    )
}

enum class ProcessingStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}
