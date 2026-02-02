package com.yourapp.application.service

import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import com.yourapp.config.BookServiceProperties

@Service
class BookService(
    private val properties: BookServiceProperties
) {
    private val books: MutableMap<BookId, Book> = ConcurrentHashMap()

    fun create(book: Book): Book {
        if (books.containsKey(book.id)) {
            throw IllegalArgumentException("Book with ID ${book.id} already exists")
        }

        if (books.size >= properties.maxBooks) {
            throw IllegalStateException("Cannot create more than ${properties.maxBooks} books")
        }

        if (properties.forbiddenTitles.contains(book.title)) {
            throw IllegalArgumentException("The title '${book.title}' is forbidden")
        }

        books[book.id] = book
        return book
    }

    fun findById(id: BookId): Book? {
        return books[id]
    }

    fun findAll(): List<Book> {
        return books.values.toList()
    }

    fun update(book: Book): Book {
        if (!books.containsKey(book.id)) {
            throw IllegalArgumentException("Book with ID ${book.id} not found")
        }
        books[book.id] = book
        return book
    }

    fun delete(id: BookId) {
        if (!books.containsKey(id)) {
            throw IllegalArgumentException("Book with ID ${id} not found")
        }
        books.remove(id)
    }

    fun exists(id: BookId): Boolean {
        return books.containsKey(id)
    }

    fun count(): Int {
        return books.size
    }

    fun clear() {
        books.clear()
    }
}
