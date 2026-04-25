package com.kille.domain.model

import com.kille.domain.exception.DomainException
import kotlin.collections.toList
import kotlin.collections.isNotEmpty
import kotlin.collections.sortBy
import kotlin.collections.toMutableList


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
    val title: String get() = _title
    val author: String get() = _author
    val language: String get() = _language
    val coverUrl: String? get() = _coverUrl
    val audio: Boolean get() = _audio
    val text: Boolean get() = _text

    private fun ensureSorting() {
        _chapters.sortBy { it.index }

        for (i in 1 until _chapters.size) {
            require(_chapters[i].index > _chapters[i-1].index) {
                "Индексы глав должны быть возрастающими"
            }
        }
    }
    fun addChapter(chapter: Chapter) {
        if (chapter.bookId != this.id) {
            throw DomainException("Глава принадлежит другой книге")
        }
        if (_chapters.any { it.index == chapter.index }) {
            throw DomainException("Глава с индексом ${chapter.index} уже существует в книге ${this.id}")
        }
        _chapters.add(chapter)
        ensureSorting()
    }

    fun hasAudio(): Boolean = _audio
    fun hasText(): Boolean = _text
    fun chapterCount(): Int = _chapters.size
    fun chapters(): List<Chapter> = _chapters.toList()

    init {
        validateTitle(_title)
        validateAuthor(_author)
        validateChapters(_chapters, this.id)
        ensureSorting()
    }

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

        private fun validateChapters(chapters: List<Chapter>, bookId: BookId? = null) {
            if (bookId != null) {
                val invalidChapters = chapters.filter { it.bookId != bookId }
                if (invalidChapters.isNotEmpty()) {
                    throw DomainException("Все главы должны принадлежать этой книге")
                }
            }
        }

        fun create(
            title: String,
            author: String,
            language: String,
            coverUrl: String? = null,
            chapters: List<Chapter>,
            audio: Boolean,
            text: Boolean
        ): Book {
            val hasAudio = chapters.any { it.hasAudio() }
            val hasText = chapters.any { it.hasText() }
            validateTitle(title)
            validateAuthor(author)
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

        fun createWithId(
            id: BookId,
            title: String,
            author: String,
            language: String,
            coverUrl: String? = null,
            chapters: List<Chapter>,
            audio: Boolean,
            text: Boolean
        ): Book {
            val hasAudio = chapters.any { it.hasAudio() }
            val hasText = chapters.any { it.hasText() }
            validateTitle(title)
            validateAuthor(author)
            validateChapters(chapters, id)
            return Book(
                id = id,
                _title = title,
                _author = author,
                _language = language,
                _coverUrl = coverUrl,
                _chapters = chapters.toMutableList(),
                _audio = hasAudio,
                _text = hasText
            )
        }

        fun restore(
            id: BookId,
            title: String,
            author: String,
            language: String,
            coverUrl: String? = null,
            chapters: List<Chapter>,
            audio: Boolean,
            text: Boolean
        ): Book {
            return Book(
                id = id,
                _title = title,
                _author = author,
                _language = language,
                _coverUrl = coverUrl,
                _chapters = chapters.toMutableList(),
                _audio = audio,
                _text = text
            )
        }
    }

    private fun validateChapterIndex(index: Int) {
        require(index in _chapters.indices) {
            "Индекс главы $index вне допустимого диапазона [0, ${_chapters.size})"
        }
    }
}

