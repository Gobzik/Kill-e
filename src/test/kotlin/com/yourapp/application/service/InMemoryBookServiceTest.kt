package com.yourapp.application.service

import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterIndex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InMemoryBookServiceTest {

    private lateinit var service: InMemoryBookService

    @BeforeEach
    fun setUp() {
        service = InMemoryBookService()
        service.clear()
    }

    @Test
    fun `CREATE - успешное создание книги`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(
            bookId = bookId,
            index = ChapterIndex(0),
            title = "Глава 1",
            text = "Текст главы 1"
        )
        val book = Book.createWithId(
            id = bookId,
            title = "Тестовая книга",
            author = "Тестовый автор",
            language = "ru",
            coverUrl = null,
            chapters = listOf(chapter),
            audio = false,
            text = true
        )

        val created = service.create(book)

        assertEquals(book.id, created.id)
        assertEquals("Тестовая книга", created.title)
        assertEquals(1, service.count())
    }

    @Test
    fun `CREATE - ошибка при дублировании ID`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(
            bookId = bookId,
            index = ChapterIndex(0),
            title = "Глава 1",
            text = "Текст"
        )
        val book = Book.createWithId(
            id = bookId,
            title = "Книга",
            author = "Автор",
            language = "ru",
            chapters = listOf(chapter),
            audio = false,
            text = true
        )
        service.create(book)

        assertThrows<IllegalArgumentException> {
            service.create(book)
        }
    }

    @Test
    fun `READ - получение книги по ID`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(
            bookId = bookId,
            index = ChapterIndex(0),
            title = "Глава 1",
            text = "Текст"
        )
        val book = Book.createWithId(
            id = bookId,
            title = "Книга",
            author = "Автор",
            language = "ru",
            chapters = listOf(chapter),
            audio = false,
            text = true
        )
        service.create(book)

        val found = service.findById(bookId)

        assertNotNull(found)
        assertEquals(bookId, found?.id)
        assertEquals("Книга", found?.title)
    }

    @Test
    fun `READ - получение всех книг`() {
        val bookId1 = BookId.generate()
        val bookId2 = BookId.generate()

        val chapter1 = Chapter.createWithText(bookId1, ChapterIndex(0), "Гл.1", "Текст 1")
        val chapter2 = Chapter.createWithText(bookId2, ChapterIndex(0), "Гл.1", "Текст 2")

        val book1 = Book.createWithId(bookId1, "Книга 1", "Автор 1", "ru", chapters = listOf(chapter1), audio = false, text = true)
        val book2 = Book.createWithId(bookId2, "Книга 2", "Автор 2", "en", chapters = listOf(chapter2), audio = false, text = true)

        service.create(book1)
        service.create(book2)

        val all = service.findAll()

        assertEquals(2, all.size)
    }

    @Test
    fun `UPDATE - успешное обновление книги`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(bookId, ChapterIndex(0), "Глава", "Текст")
        val book = Book.createWithId(bookId, "Старое название", "Автор", "ru", chapters = listOf(chapter), audio = false, text = true)
        service.create(book)

        val updatedBook = Book.restore(bookId, "Новое название", "Автор", "ru", chapters = listOf(chapter), audio = false, text = true)

        val result = service.update(updatedBook)

        assertEquals("Новое название", result.title)
        assertEquals("Новое название", service.findById(bookId)?.title)
    }

    @Test
    fun `UPDATE - ошибка при обновлении несуществующей книги`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(bookId, ChapterIndex(0), "Глава", "Текст")
        val book = Book.createWithId(bookId, "Книга", "Автор", "ru", chapters = listOf(chapter), audio = false, text = true)

        assertThrows<IllegalArgumentException> {
            service.update(book)
        }
    }

    @Test
    fun `DELETE - успешное удаление книги`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(bookId, ChapterIndex(0), "Глава", "Текст")
        val book = Book.createWithId(bookId, "Книга", "Автор", "ru", chapters = listOf(chapter), audio = false, text = true)
        service.create(book)
        assertEquals(1, service.count())

        service.delete(bookId)

        assertEquals(0, service.count())
        assertNull(service.findById(bookId))
    }

    @Test
    fun `DELETE - ошибка при удалении несуществующей книги`() {
        val bookId = BookId.generate()

        assertThrows<IllegalArgumentException> {
            service.delete(bookId)
        }
    }

    @Test
    fun `exists - проверка существования книги`() {
        val bookId = BookId.generate()
        val chapter = Chapter.createWithText(bookId, ChapterIndex(0), "Глава", "Текст")
        val book = Book.createWithId(bookId, "Книга", "Автор", "ru", chapters = listOf(chapter), audio = false, text = true)

        assertFalse(service.exists(bookId))
        service.create(book)
        assertTrue(service.exists(bookId))
    }

    @Test
    fun `count - подсчет количества книг`() {
        assertEquals(0, service.count())

        val bookId1 = BookId.generate()
        val bookId2 = BookId.generate()
        val chapter1 = Chapter.createWithText(bookId1, ChapterIndex(0), "Гл", "Т")
        val chapter2 = Chapter.createWithText(bookId2, ChapterIndex(0), "Гл", "Т")

        service.create(Book.createWithId(bookId1, "Книга 1", "Автор", "ru", chapters = listOf(chapter1), audio = false, text = true))
        assertEquals(1, service.count())

        service.create(Book.createWithId(bookId2, "Книга 2", "Автор", "ru", chapters = listOf(chapter2), audio = false, text = true))
        assertEquals(2, service.count())
    }
}
