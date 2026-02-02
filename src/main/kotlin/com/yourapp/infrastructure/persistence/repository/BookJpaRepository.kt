package com.yourapp.infrastructure.persistence.repository

import com.yourapp.infrastructure.persistence.entity.BookEntityJpa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Spring Data JPA Repository для BookEntityJpa.
 *
 * Автоматически реализуется Spring Data JPA.
 * Предоставляет CRUD операции и кастомные запросы.
 */
@Repository
interface BookJpaRepository : JpaRepository<BookEntityJpa, UUID> {

    /**
     * Поиск книг по автору.
     */
    fun findByAuthor(author: String): List<BookEntityJpa>

    /**
     * Поиск книг по языку.
     */
    fun findByLanguage(language: String): List<BookEntityJpa>

    /**
     * Поиск книг, у которых есть аудио.
     */
    fun findByHasAudioTrue(): List<BookEntityJpa>

    /**
     * Поиск книг, у которых есть текст.
     */
    fun findByHasTextTrue(): List<BookEntityJpa>

    /**
     * Поиск книг по автору и языку.
     */
    fun findByAuthorAndLanguage(author: String, language: String): List<BookEntityJpa>
}