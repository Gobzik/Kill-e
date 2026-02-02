package com.yourapp.infrastructure.persistence.adapter

import com.yourapp.domain.exception.EntityNotFoundException
import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterId
import com.yourapp.domain.repository.BookRepository
import com.yourapp.infrastructure.persistence.entity.BookEntityJpa
import com.yourapp.infrastructure.persistence.entity.ChapterEntityJpa
import com.yourapp.infrastructure.persistence.repository.BookJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

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

    // ========== Mapper Methods ==========

    private fun toJpaEntity(domain: Book): BookEntityJpa {
        // Создаем книгу без дат из domain (их нет)
        val bookJpa = BookEntityJpa(
            id = domain.id.value,
            title = domain.title,
            author = domain.author,
            language = domain.language,
            coverUrl = domain.coverUrl,
            hasAudio = domain.hasAudio(),
            hasText = domain.hasText(),
            createdAt = LocalDateTime.now(), // Генерируем новую дату
            updatedAt = LocalDateTime.now()
        )

        // Создаем главы
        val chaptersJpa = domain.chapters().map { chapter ->
            ChapterEntityJpa(
                id = chapter.id.value,
                book = bookJpa,
                title = chapter.title,
                content = chapter.content,
                index = chapter.index,
                audioUrl = chapter.audioUrl,
                createdAt = LocalDateTime.now(), // Генерируем новую дату
                updatedAt = LocalDateTime.now()
            )
        }

        bookJpa.chapters = chaptersJpa
        return bookJpa
    }

    private fun toDomain(jpa: BookEntityJpa): Book {
        // Создаем главы из JPA
        val chapters = jpa.chapters.map { chapterJpa ->
            // Используем create, так как у Chapter нет restore без дат
            Chapter.create(
                title = chapterJpa.title,
                content = chapterJpa.content,
                index = chapterJpa.index,
                audioUrl = chapterJpa.audioUrl,
                bookId = BookId(jpa.id) // Передаем bookId
            )
        }

        // Используем create для книги, игнорируя даты из JPA
        return Book.create(
            title = jpa.title,
            author = jpa.author,
            language = jpa.language,
            coverUrl = jpa.coverUrl,
            chapters = chapters,
            audio = jpa.hasAudio,
            text = jpa.hasText
        ).copyWithId(BookId(jpa.id)) // Восстанавливаем ID
    }
}

}
