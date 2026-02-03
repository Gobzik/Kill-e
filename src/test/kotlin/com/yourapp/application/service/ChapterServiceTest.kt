package com.yourapp.application.usecase.chapter

import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterId
import com.yourapp.domain.model.ChapterIndex
import com.yourapp.domain.repository.ChapterRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class InMemoryChapterRepository : ChapterRepository {
    private val chapters = mutableMapOf<ChapterId, Chapter>()
    private val bookIndexMap = mutableMapOf<BookId, MutableList<Chapter>>()

    override fun existsByBookIdAndIndex(bookId: BookId, index: Int): Boolean {
        return bookIndexMap[bookId]?.any { it.index.value == index } ?: false
    }

    override fun save(chapter: Chapter): Chapter {
        chapters[chapter.id] = chapter

        // Добавляем в индекс по книге
        val bookChapters = bookIndexMap.getOrPut(chapter.bookId) { mutableListOf() }
        bookChapters.removeAll { it.id == chapter.id }
        bookChapters.add(chapter)

        return chapter
    }

    override fun findById(id: ChapterId): Optional<Chapter> {
        return Optional.ofNullable(chapters[id])
    }

    override fun findAllByBookId(bookId: BookId): List<Chapter> {
        return bookIndexMap[bookId]?.toList() ?: emptyList()
    }

    override fun findByBookIdOrderByIndex(bookId: BookId): List<Chapter> {
        return bookIndexMap[bookId]
            ?.sortedBy { it.index.value }
            ?.toList() ?: emptyList()
    }

    override fun deleteById(id: ChapterId) {
        val chapter = chapters[id]
        if (chapter != null) {
            chapters.remove(id)
            bookIndexMap[chapter.bookId]?.removeIf { it.id == id }
        }
    }

    override fun existsById(id: ChapterId): Boolean {
        return chapters.containsKey(id)
    }

    override fun countByBookId(bookId: BookId): Int {
        return bookIndexMap[bookId]?.size ?: 0
    }

}

class CreateChapterUseCaseTestWithoutMock {

    private lateinit var chapterRepository: InMemoryChapterRepository
    private lateinit var useCase: CreateChapterUseCase

    private var bookId: BookId? = null

    private val chapterIndex = ChapterIndex(1)
    private val title = "Глава 1"
    private val text = "Текст главы 1"
    private val audioUrl = "https://example.com/audio.mp3"
    private val timingUrl = "https://example.com/timing.json"

    @BeforeEach
    fun setUp() {
        chapterRepository = InMemoryChapterRepository()
        useCase = CreateChapterUseCase(chapterRepository)
        bookId = BookId.generate() // Инициализируем здесь
    }

    @Test
    fun `CREATE - успешное создание главы с текстом`() {
        // Используем !! так как bookId гарантированно инициализирован в setUp
        val bookId = this.bookId!!

        // Act
        val result = useCase.execute(
            bookId = bookId,
            index = chapterIndex,
            title = title,
            text = text,
            audioUrl = null,
            timingUrl = null
        )

        // Assert
        assertEquals(bookId, result.bookId)
        assertEquals(chapterIndex, result.index)
        assertEquals(title, result.title)
        assertEquals(text, result.text)
        assertNull(result.audioUrl)

        // Проверяем через методы репозитория
        assertTrue(chapterRepository.existsById(result.id))
        assertEquals(Optional.of(result), chapterRepository.findById(result.id))
        assertEquals(1, chapterRepository.countByBookId(bookId))

        val bookChapters = chapterRepository.findAllByBookId(bookId)
        assertEquals(1, bookChapters.size)
        assertEquals(result.id, bookChapters[0].id)
    }
}
