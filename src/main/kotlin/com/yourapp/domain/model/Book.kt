package com.yourapp.domain.model

import com.yourapp.domain.exception.DomainException
import java.time.LocalDateTime
import java.util.UUID
import kotlin.collections.toList
import kotlin.collections.isNotEmpty
import kotlin.collections.all
import kotlin.collections.toMutableList
import kotlin.collections.sortBy
import com.yourapp.domain.model.BookId
// bookID беру из Chapter
 class Book private constructor(
    val id: BookId,
    val _title: String,
    val _author: String,
    val _language: String,
    val _coverUrl: String?,
    private var _chapters: MutableList<Chapter>,
    val _audio: Boolean,
    val _text: Boolean,

) {
    // ========== Публичные read-only свойства (геттеры) ==========
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
        validateChapters(_chapters, this.id)
        _chapters.add(chapter)
        ensureSorting()
    }
    fun getChapter(index: Int): Chapter {
        validateChapterIndex(index)
        return _chapters[index]
    }
    fun hasAudio(): Boolean = _audio
    fun hasText(): Boolean = _text
    fun chapterCount(): Int = _chapters.size
    fun chapters(): List<Chapter> = _chapters.toList()
    // ========== ДОМЕННЫЕ ИНВАРИАНТЫ ==========
    init {
        // Используем вынесенные методы валидации
        validateTitle(_title)
        validateAuthor(_author)
        validateChapters(_chapters, this.id)
        ensureSorting()
    }
    // ========== Метод для создания ==========
    companion object {

        private fun validateTitle(title: String) {
            if (title.isBlank()) {
                throw DomainException("Заголовок книги не может быть пустым")
            }
            if (title.length > 500) {
                throw DomainException("Название книги слишком длинное (максимум 500 символов)")
            }
        }

        private fun validateAuthor(author: String) {
            if (author.isBlank()) {
                throw DomainException("Автор книги не может быть пустым")
            }
            if (author.length > 200) {
                throw DomainException("Имя автора слишком длинное (максимум 200 символов)")
            }
        }
        private fun validateChapterIndex(index: Int) {
            require(index in 0 until _chapters.size) {
                "Индекс главы $index вне допустимого диапазона [0, ${_chapters.size})"
            }
        }
        private fun validateChapters(chapters: List<Chapter>, bookId: BookId? = null) {
            if (chapters.isEmpty()) {
                throw DomainException("Книга должна содержать минимум 1 главу")
            }

            // Проверяем принадлежность глав книге
            if (bookId != null) {
                val invalidChapters = chapters.filter { it.bookId != bookId }
                if (invalidChapters.isNotEmpty()) {
                    throw DomainException("Все главы должны принадлежать этой книге")
                }
            }
        fun create(
            title: String,
            author: String,
            language: String,
            coverUrl: String? = null,
            chapters: List<Chapter>,
            audio: Boolean,
            text: Boolean,
        ): Book {
            val hasAudio = chapters.any { it.hasAudio }
            val hasText = chapters.any { it.hasText }
            validateTitle(title)
            validateAuthor(author)
            validateChapters(chapters)
            return Book(
                id = BookId.generate(),
                _title = title,
                _author = author,
                _language = language,
                _coverUrl = coverUrl,
                _chapters = chapters.toMutableList(),
                _audio = hasAudio,
                _text = hasText

            )
        }
    }

    }
    }
 }

