package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.BookEntityJpa
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BookJpaRepository : JpaRepository<BookEntityJpa, UUID> {

    fun findByAuthor(author: String): List<BookEntityJpa>
    fun findByLanguage(language: String): List<BookEntityJpa>
    fun findByHasAudioTrue(): List<BookEntityJpa>
    fun findByHasTextTrue(): List<BookEntityJpa>
    fun findByAuthorAndLanguage(author: String, language: String): List<BookEntityJpa>
}