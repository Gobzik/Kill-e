package com.kille.domain.repository

import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
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
    fun findByIdAndBookId(chapterId: Long, bookId: Long): Chapter?
}
