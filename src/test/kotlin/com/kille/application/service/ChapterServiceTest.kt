package com.kille.application.service

import com.kille.config.BookServiceProperties
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterIndex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ChapterServiceTest {

    private lateinit var service: ChapterService
    private val bookId = BookId.generate()
    private val anotherBookId = BookId.generate()

    @BeforeEach
    fun setUp() {
        val properties = BookServiceProperties(
            maxBooks = 100,
            forbiddenTitles = emptyList(),
            maxChaptersPerBook = 3,
            maxChapterLength = 50
        )
        service = ChapterService(properties)
        service.clear()
    }

    private fun createChapter(
        bookId: BookId = this.bookId,
        index: Int = 0,
        title: String = "Глава $index",
        text: String = "Текст главы $index"
    ): Chapter {
        return Chapter.createWithText(
            bookId = bookId,
            index = ChapterIndex(index),
            title = title,
            text = text
        )
    }

    @Test
    fun `CREATE - успешное создание главы`() {
        val chapter = createChapter()

        val created = service.create(chapter)

        assertEquals(chapter.id, created.id)
        assertEquals("Глава 0", created.title)
        assertEquals(bookId, created.bookId)
    }

    @Test
    fun `CREATE - ошибка при дублировании ID`() {
        val chapter = createChapter()
        service.create(chapter)

        assertThrows<IllegalArgumentException> {
            service.create(chapter)
        }
    }

    @Test
    fun `CREATE - ошибка при превышении лимита глав в книге`() {
        // Создаем максимальное количество глав
        for (i in 0 until 3) {
            val chapter = createChapter(index = i)
            service.create(chapter)
        }

        // Попытка создать главу сверх лимита
        val extraChapter = createChapter(index = 3, title = "Лишняя глава")

        assertThrows<IllegalStateException> {
            service.create(extraChapter)
        }
    }

    @Test
    fun `CREATE - ошибка при превышении максимальной длины текста`() {
        val longText = "x".repeat(51) // Максимум 50
        val chapter = createChapter(text = longText)

        assertThrows<IllegalArgumentException> {
            service.create(chapter)
        }
    }

    @Test
    fun `READ - получение главы по ID`() {
        val chapter = createChapter(title = "Тестовая глава")
        service.create(chapter)

        val found = service.findById(chapter.id)

        assertNotNull(found)
        assertEquals(chapter.id, found?.id)
        assertEquals("Тестовая глава", found?.title)
    }

    @Test
    fun `READ - получение глав по ID книги`() {
        val chapter1 = createChapter(index = 0, title = "Глава 1")
        val chapter2 = createChapter(index = 1, title = "Глава 2")
        val chapterOther = createChapter(bookId = anotherBookId, title = "Глава другой книги")

        service.create(chapter1)
        service.create(chapter2)
        service.create(chapterOther)

        val chapters = service.findByBookId(bookId)

        assertEquals(2, chapters.size)
        assertEquals("Глава 1", chapters[0].title)
        assertEquals("Глава 2", chapters[1].title)
    }

    @Test
    fun `UPDATE - ошибка при обновлении несуществующей главы`() {
        val nonExistentChapter = createChapter()

        assertThrows<IllegalArgumentException> {
            service.update(nonExistentChapter)
        }
    }

    @Test
    fun `DELETE - успешное удаление главы`() {
        val chapter = createChapter()
        service.create(chapter)

        assertTrue(service.exists(chapter.id))

        service.delete(chapter.id)

        assertFalse(service.exists(chapter.id))
    }

    @Test
    fun `countByBookId - подсчет глав в книге`() {
        assertEquals(0, service.countByBookId(bookId))

        val chapter1 = createChapter(index = 0)
        val chapter2 = createChapter(index = 1)

        service.create(chapter1)
        assertEquals(1, service.countByBookId(bookId))

        service.create(chapter2)
        assertEquals(2, service.countByBookId(bookId))
    }
}