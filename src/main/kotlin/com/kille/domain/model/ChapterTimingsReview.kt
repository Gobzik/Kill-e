package com.kille.domain.model

import java.time.LocalDateTime

data class ChapterTimingsReview(
    val chapterId: ChapterId,
    val bookId: BookId,
    val audioS3Key: String,
    val timingsJson: String,
    val status: ChapterTimingsReviewStatus,
    val wordCount: Int,
    val durationMs: Long?,
    val timingUrl: String? = null,
    val errorMessage: String? = null,
    val createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime
) {
    val updatedAt: LocalDateTime get() = _updatedAt

    companion object {
        fun create(
            chapterId: ChapterId,
            bookId: BookId,
            audioS3Key: String,
            timingsJson: String,
            wordCount: Int,
            durationMs: Long?
        ): ChapterTimingsReview {
            val now = LocalDateTime.now()
            return ChapterTimingsReview(
                chapterId = chapterId,
                bookId = bookId,
                audioS3Key = audioS3Key,
                timingsJson = timingsJson,
                status = ChapterTimingsReviewStatus.PENDING,
                wordCount = wordCount,
                durationMs = durationMs,
                createdAt = now,
                _updatedAt = now
            )
        }
    }

    fun markReviewing(): ChapterTimingsReview = copy(status = ChapterTimingsReviewStatus.REVIEWING, _updatedAt = LocalDateTime.now())

    fun complete(timingUrl: String): ChapterTimingsReview = copy(
        status = ChapterTimingsReviewStatus.COMPLETED,
        timingUrl = timingUrl,
        errorMessage = null,
        _updatedAt = LocalDateTime.now()
    )

    fun updateTimings(timingsJson: String): ChapterTimingsReview = copy(
        timingsJson = timingsJson,
        _updatedAt = LocalDateTime.now()
    )
}

enum class ChapterTimingsReviewStatus {
    PENDING,
    REVIEWING,
    COMPLETED
}
