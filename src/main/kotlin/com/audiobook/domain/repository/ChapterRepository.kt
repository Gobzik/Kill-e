package com.audiobook.domain.repository

import com.audiobook.domain.model.BookId
import com.audiobook.domain.model.Chapter
import com.audiobook.domain.model.ChapterId
import java.util.*

interface ChapterRepository {
    fun findById(id: ChapterId): Optional<Chapter>
    fun findAllByBookId(bookId: BookId): List<Chapter>
    fun findByBookIdOrderByIndex(bookId: BookId): List<Chapter>
    fun save(chapter: Chapter): Chapter
    fun deleteById(id: ChapterId)
    fun existsById(id: ChapterId): Boolean
    fun existsByBookIdAndIndex(bookId: BookId, index: Int): Boolean
    fun countByBookId(bookId: BookId): Int
}