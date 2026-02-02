package com.yourapp.infrastructure.persistence.repository

import com.yourapp.infrastructure.persistence.entity.ChapterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ChapterJpaRepository : JpaRepository<ChapterEntity, UUID> {

    fun findByBookId(bookId: UUID): List<ChapterEntity>

    fun findByBookIdOrderByIndexAsc(bookId: UUID): List<ChapterEntity>

    fun existsByBookIdAndIndex(bookId: UUID, index: Int): Boolean

    @Query("SELECT COUNT(c) FROM ChapterEntity c WHERE c.bookId = :bookId")
    fun countByBookId(@Param("bookId") bookId: UUID): Int
}