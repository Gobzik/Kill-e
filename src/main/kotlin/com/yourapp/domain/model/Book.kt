package com.yourapp.domain.model

import com.yourapp.domain.exception.DomainException
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.toList
import kotlin.collections.isNotEmpty
import kotlin.collections.all
import kotlin.collections.toMutableList
import kotlin.collections.sortBy
// bookID беру из Chapter
 class Book(
    val _id: UUID,
    val _title: String,
    val _author: String,
    val _language: String,
    val _coverUrl: String?,
    private var _chapters: MutableList<Chapter>,
    val _audio: Boolean,
    val _text: Boolean,

) {
    // ========== Публичные read-only свойства (геттеры) ==========
    val id: UUID get() = _id
    val title: String get() = _title
    val author: String get() = _author
    val language: String get() = _language
    val coverUrl: String? get() = _coverUrl
    val audio: Boolean get() = _audio
    val text: Boolean get() = _text

    // ========== Функции ==========
    private fun ensureSorting() {
        _chapters.sortBy { it.index }

        // Проверка: индексы должны быть последовательными
        for (i in 1 until _chapters.size) {
            require(_chapters[i].index > _chapters[i-1].index) {
                "Индексы глав должны быть возрастающими"
            }
        }
    }
    fun addChapter(chapter: Chapter) {
        require(chapter.bookId == this._id) {
            "Глава должна принадлежать этой книге"
        }
        _chapters.add(chapter)
        ensureSorting()
    }
    fun getChapter(index: Int): Chapter {
        require(index in 0 until _chapters.size) {
            "Индекс $index вне диапазона глав [0, ${_chapters.size})"
        }
        return _chapters[index]
    }
    fun hasAudio(): Boolean = _audio
    fun hasText(): Boolean = _text
    fun chapterCount(): Int = _chapters.size
    fun chapters(): List<Chapter> = _chapters.toList()
    // ========== ДОМЕННЫЕ ИНВАРИАНТЫ ==========
    init {
        require(_title.isNotBlank()) { "Название книги не может быть пустым" }
        require(_chapters.isNotEmpty()) { "Книга должна содержать минимум 1 главу" }
        require(_chapters.all { it.bookId == this._id }) { "Все главы должны принадлежать этой книге" }
        ensureSorting()
    }
    // ========== Метод для создания ==========
    companion object {
        fun create(
            id: UUID = UUID.randomUUID(),
            title: String,
            author: String,
            language: String,
            coverUrl: String? = null,
            chapters: List<Chapter>
        ): Book {
            return Book(
                _id = id,
                _title = title,
                _author = author,
                _language = language,
                _coverUrl = coverUrl,
                _chapters = chapters.toMutableList()
            )
        }
    }
    }
 }
