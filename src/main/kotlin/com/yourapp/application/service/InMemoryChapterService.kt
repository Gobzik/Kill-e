package com.yourapp.application.service

import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterId
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class InMemoryChapterService {

    private val chapters: MutableMap<ChapterId, Chapter> = ConcurrentHashMap()

    fun create(chapter: Chapter): Chapter {
        if (chapters.containsKey(chapter.id)) {
            throw IllegalArgumentException("Chapter with ID ${chapter.id} already exists")
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
