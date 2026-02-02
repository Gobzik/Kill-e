package com.yourapp.infrastructure.persistence.adapter

import com.yourapp.domain.exception.EntityNotFoundException
import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterIndex
import com.yourapp.domain.repository.BookRepository
import com.yourapp.infrastructure.persistence.entity.BookEntityJpa
import com.yourapp.infrastructure.persistence.entity.ChapterEntityJpa
import com.yourapp.infrastructure.persistence.repository.BookJpaRepository
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
            hasAudio = domain.hasAudio(),
            hasText = domain.hasText(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val chaptersJpa = domain.chapters().map { chapter ->
            ChapterEntityJpa(
                id = chapter.id.value,
                book = bookJpa,
                title = chapter.title ?: "",
                content = chapter.text ?: "",
                index = chapter.index.value,
                audioUrl = chapter.audioUrl,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }

        bookJpa.chapters = chaptersJpa as MutableList<ChapterEntityJpa>
        return bookJpa
    }

    private fun toDomain(jpa: BookEntityJpa): Book {
        val chapters = jpa.chapters.map { chapterJpa ->
            Chapter.restore(
                id = com.yourapp.domain.model.ChapterId.fromString(chapterJpa.id.toString()),
                bookId = BookId(jpa.id),
                index = ChapterIndex(chapterJpa.index),
                title = chapterJpa.title,
                text = chapterJpa.content,
                audioUrl = chapterJpa.audioUrl,
                timingUrl = null,
                createdAt = chapterJpa.createdAt,
                updatedAt = chapterJpa.updatedAt,
                durationMs = null
            )
        }

        return Book.restore(
            id = BookId(jpa.id),
            title = jpa.title,
            author = jpa.author,
            language = jpa.language,
            coverUrl = jpa.coverUrl,
            chapters = chapters,
            audio = jpa.hasAudio,
            text = jpa.hasText
        )
    }
}

