package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.BookId
import com.kille.domain.repository.ChapterRepository
import com.kille.infrastructure.persistence.entity.ChapterJpaEntity
import com.kille.infrastructure.persistence.repository.ChapterJpaRepository
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterIndex
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ChapterRepositoryAdapter(
    private val jpaRepository: ChapterJpaRepository
) : ChapterRepository {

    override fun findById(id: ChapterId): Optional<Chapter> {
        return jpaRepository.findById(id.value)
            .map { it.toDomain() }
    }

    override fun findAllByBookId(bookId: BookId): List<Chapter> {
        return jpaRepository.findByBookId(bookId.value)
            .map { it.toDomain() }
    }

    override fun findByBookIdOrderByIndex(bookId: BookId): List<Chapter> {
        return jpaRepository.findByBookIdOrderByIndexAsc(bookId.value)
            .map { it.toDomain() }
    }

    override fun save(chapter: Chapter): Chapter {
        val entity = chapter.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: ChapterId) {
        jpaRepository.deleteById(id.value)
    }

    override fun existsById(id: ChapterId): Boolean {
        return jpaRepository.existsById(id.value)
    }

    override fun existsByBookIdAndIndex(bookId: BookId, index: Int): Boolean {
        return jpaRepository.existsByBookIdAndIndex(bookId.value, index)
    }

    override fun countByBookId(bookId: BookId): Int {
        return jpaRepository.countByBookId(bookId.value)
    }
}

fun ChapterJpaEntity.toDomain(): Chapter {
    return Chapter.restore(
        id = ChapterId.fromString(id.toString()),
        bookId = BookId.fromString(bookId.toString()),
        index = ChapterIndex(index),
        title = title,
        text = text,
        audioUrl = audioUrl,
        timingUrl = timingUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        durationMs = durationMs
    )
}

fun Chapter.toEntity(): ChapterJpaEntity {
    return ChapterJpaEntity(
        id = id.value,
        bookId = bookId.value,
        index = index.value,
        title = title,
        text = text,
        audioUrl = audioUrl,
        timingUrl = timingUrl,
        durationMs = durationMs,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}