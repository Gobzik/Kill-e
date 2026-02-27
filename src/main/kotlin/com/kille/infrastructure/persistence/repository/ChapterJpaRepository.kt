package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.ChapterJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ChapterJpaRepository : JpaRepository<ChapterJpaEntity, UUID> {

    fun findByBookId(bookId: UUID): List<ChapterJpaEntity>
    fun findByBookIdOrderByIndexAsc(bookId: UUID): List<ChapterJpaEntity>
    fun existsByBookIdAndIndex(bookId: UUID, index: Int): Boolean
    @Query("SELECT COUNT(c) FROM ChapterJpaEntity c WHERE c.bookId = :bookId")
    fun countByBookId(@Param("bookId") bookId: UUID): Int
}