package com.yourapp.domain.repository

import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId

interface BookRepository {
    fun save(book: Book): Book
    fun findById(id: BookId): Book?
    fun findAll(): List<Book>
    fun delete(id: BookId)
}