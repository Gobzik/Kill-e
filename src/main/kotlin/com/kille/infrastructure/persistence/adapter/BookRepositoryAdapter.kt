package com.kille.infrastructure.persistence.adapter

import com.kille.domain.exception.EntityNotFoundException
import com.kille.domain.model.Book
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
import com.kille.domain.model.ChapterIndex
import com.kille.domain.repository.BookRepository
import com.kille.infrastructure.persistence.entity.BookEntityJpa
import com.kille.infrastructure.persistence.entity.ChapterJpaEntity
import com.kille.infrastructure.persistence.repository.BookJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional
class BookRepositoryAdapter(
    private val jpaRepository: BookJpaRepository
) : BookRepository {

    override fun save(book: Book): Book {
        val jpaEntity = toJpaEntity(book)
        val saved = jpaRepository.save(jpaEntity)
        return toDomain(saved)
    }

    @Transactional(readOnly = true)
    override fun findById(id: BookId): Book? {
        return jpaRepository.findById(id.value)
            .map { toDomain(it) }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Book> {
        return jpaRepository.findAll()
            .map { toDomain(it) }
    }

    override fun delete(id: BookId) {
        if (!jpaRepository.existsById(id.value)) {
            throw EntityNotFoundException("Book", id.toString())
        }
        jpaRepository.deleteById(id.value)
    }


    private fun toJpaEntity(domain: Book): BookEntityJpa {
        val bookJpa = BookEntityJpa(
            id = domain.id.value,
            title = domain.title,
            author = domain.author,
            language = domain.language,
            coverUrl = domain.coverUrl,
            hasAudio = domain.audio,
            hasText = domain.text,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val chaptersJpa = domain.chapters().map { chapter ->
            ChapterJpaEntity(
                id = chapter.id.value,
                bookId = bookJpa.id,
                title = chapter.title,
                text = chapter.text,
                index = chapter.index.value,
                audioUrl = chapter.audioUrl,
                timingUrl = chapter.timingUrl,
                durationMs = chapter.durationMs,
                createdAt = chapter.createdAt,
                updatedAt = chapter.updatedAt
            )
        }

        bookJpa.chapters = chaptersJpa.toMutableList()
        return bookJpa
    }

    private fun toDomain(jpa: BookEntityJpa): Book {
        val chapters = jpa.chapters.map { chapterJpa ->
            Chapter.restore(
                id = ChapterId.fromString(chapterJpa.id.toString()),
                bookId = BookId(jpa.id),
                index = ChapterIndex(chapterJpa.index),
                title = chapterJpa.title,
                text = chapterJpa.text,
                audioUrl = chapterJpa.audioUrl,
                timingUrl = chapterJpa.timingUrl,
                createdAt = chapterJpa.createdAt,
                updatedAt = chapterJpa.updatedAt,
                durationMs = chapterJpa.durationMs
            )
        }

        val hasAudio = chapters.any { it.hasAudio() }
        val hasText = chapters.any { it.hasText() }

        return Book.restore(
            id = BookId(jpa.id),
            title = jpa.title,
            author = jpa.author,
            language = jpa.language,
            coverUrl = jpa.coverUrl,
            chapters = chapters,
            audio = hasAudio,
            text = hasText
        )
    }
}
