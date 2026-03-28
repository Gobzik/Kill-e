package com.kille.application.service

import com.kille.config.BookServiceProperties
import com.kille.domain.model.BookId
import com.kille.domain.model.Chapter
import com.kille.domain.model.ChapterId
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ChapterService(
    private val properties: BookServiceProperties
) {

    private val chapters: MutableMap<ChapterId, Chapter> = ConcurrentHashMap()

    fun create(chapter: Chapter): Chapter {
        if (chapters.containsKey(chapter.id)) {
            throw IllegalArgumentException("Chapter with ID ${chapter.id} already exists")
        }

        val chaptersInBook = chapters.values.count { it.bookId == chapter.bookId }
        if (chaptersInBook >= properties.maxChaptersPerBook) {
            throw IllegalStateException(
                "Cannot create more than ${properties.maxChaptersPerBook} chapters for book ID ${chapter.bookId}"
            )
        }

        if (chapter.text != null && chapter.text!!.length > properties.maxChapterLength) {
            throw IllegalArgumentException(
                "Chapter text length exceeds maximum of ${properties.maxChapterLength} characters"
            )
        }

        chapters[chapter.id] = chapter
        return chapter
    }

    fun findById(id: ChapterId): Chapter? {
        return chapters[id]
    }

    fun findByBookId(bookId: BookId): List<Chapter> {
        return chapters.values
            .filter { it.bookId == bookId }
            .sortedBy { it.index.value }
    }

    fun findAll(): List<Chapter> {
        return chapters.values.toList()
    }

    fun update(chapter: Chapter): Chapter {
        if (!chapters.containsKey(chapter.id)) {
            throw IllegalArgumentException("Chapter with ID ${chapter.id} not found")
        }
        chapters[chapter.id] = chapter
        return chapter
    }

    fun delete(id: ChapterId) {
        if (!chapters.containsKey(id)) {
            throw IllegalArgumentException("Chapter with ID ${id} not found")
        }
        chapters.remove(id)
    }

    fun deleteByBookId(bookId: BookId) {
        val chapterIds = chapters.values
            .filter { it.bookId == bookId }
            .map { it.id }
        chapterIds.forEach { chapters.remove(it) }
    }

    fun exists(id: ChapterId): Boolean {
        return chapters.containsKey(id)
    }

    fun countByBookId(bookId: BookId): Int {
        return chapters.values.count { it.bookId == bookId }
    }

    fun clear() {
        chapters.clear()
    }
}
