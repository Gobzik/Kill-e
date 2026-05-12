package com.kille.domain.repository

import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterTimingsReview

interface ChapterTimingsReviewRepository {
    fun save(review: ChapterTimingsReview): ChapterTimingsReview
    fun findByChapterId(chapterId: ChapterId): ChapterTimingsReview?
    fun findAll(): List<ChapterTimingsReview>
}

