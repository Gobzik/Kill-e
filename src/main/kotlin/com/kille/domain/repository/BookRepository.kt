package com.kille.domain.repository

import com.kille.domain.model.Book
import com.kille.domain.model.BookId

interface BookRepository {
    fun save(book: Book): Book
    fun findById(id: BookId): Book?
    fun findAll(): List<Book>
    fun delete(id: BookId)
}