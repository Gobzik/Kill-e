package com.yourapp.domain.model

import com.yourapp.domain.exception.DomainException
import java.time.LocalDateTime

/**
 * Chapter - доменная сущность главы.
 *
 * Responsibilities:
 * - Хранить контент главы (текст/аудио/тайминги)
 * - Определять доступность воспроизведения
 * - Инкапсулировать правила валидации главы
 */
data class Chapter private constructor(
    val id: ChapterId,
    val bookId: BookId,
    private var _index: ChapterIndex,
    private var _title: String?,
    private var _text: String?,
    private var _audioUrl: String?,
    private var _timingUrl: String?,
    val createdAt: LocalDateTime,
    private var _updatedAt: LocalDateTime,
    private var _durationMs: Long? = null
) {

    // Публичные геттеры
    val index: ChapterIndex get() = _index
    val title: String? get() = _title
    val text: String? get() = _text
    val audioUrl: String? get() = _audioUrl
    val timingUrl: String? get() = _timingUrl
    val updatedAt: LocalDateTime get() = _updatedAt
    val durationMs: Long? get() = _durationMs

    companion object {
        /**
         * Фабричный метод для создания главы с текстом.
         */
        fun createWithText(
            bookId: BookId,
            index: ChapterIndex,
            title: String? = null,
            text: String
        ): Chapter {
            validateText(text)
            validateTitle(title)

            val now = LocalDateTime.now()
            return Chapter(
                id = ChapterId.generate(),
                bookId = bookId,
                _index = index,
                _title = title?.trim(),
                _text = text.trim(),
                _audioUrl = null,
                _timingUrl = null,
                createdAt = now,
                _updatedAt = now
            )
        }

        /**
         * Фабричный метод для восстановления из БД.
         */
        fun restore(
            id: ChapterId,
            bookId: BookId,
            index: ChapterIndex,
            title: String?,
            text: String?,
            audioUrl: String?,
            timingUrl: String?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
            durationMs: Long?
        ): Chapter {
            return Chapter(
                id = id,
                bookId = bookId,
                _index = index,
                _title = title,
                _text = text,
                _audioUrl = audioUrl,
                _timingUrl = timingUrl,
                createdAt = createdAt,
                _updatedAt = updatedAt,
                _durationMs = durationMs
            )
        }

        private fun validateText(text: String) {
            if (text.isBlank()) {
                throw DomainException("Chapter text cannot be empty")
            }
        }

        private fun validateTitle(title: String?) {
            title?.let {
                if (it.length > 500) {
                    throw DomainException("Chapter title too long (max 500 characters)")
                }
            }
        }
    }

    // ========== Domain Methods ==========

    fun hasText(): Boolean = !_text.isNullOrBlank()
    fun hasAudio(): Boolean = !_audioUrl.isNullOrBlank()
    fun hasTimings(): Boolean = !_timingUrl.isNullOrBlank()
    fun isPlayable(): Boolean = hasText() || hasAudio()
    fun durationAvailable(): Boolean = _durationMs != null || hasTimings()

    // ========== Business Methods ==========

    fun updateText(newText: String): Chapter {
        if (newText.isBlank()) {
            throw DomainException("Chapter text cannot be empty")
        }
        if (newText == _text) return this

        return copy(
            _text = newText.trim(),
            _updatedAt = LocalDateTime.now()
        )
    }

    fun addAudio(audioUrl: String, timingUrl: String? = null): Chapter {
        if (audioUrl.isBlank()) {
            throw DomainException("Audio URL cannot be empty")
        }

        return copy(
            _audioUrl = audioUrl.trim(),
            _timingUrl = timingUrl?.trim(),
            _updatedAt = LocalDateTime.now()
        )
    }

    fun updateDuration(durationMs: Long): Chapter {
        if (durationMs <= 0) {
            throw DomainException("Duration must be positive")
        }

        return copy(
            _durationMs = durationMs,
            _updatedAt = LocalDateTime.now()
        )
    }

    // ========== Validation ==========

    private fun validate() {
        require(_index.value >= 0) { "Chapter index must be >= 0" }

        val hasContent = !_text.isNullOrBlank() || !_audioUrl.isNullOrBlank()
        require(hasContent) { "Chapter must have either text or audio content" }

        if (!_timingUrl.isNullOrBlank()) {
            require(!_audioUrl.isNullOrBlank()) {
                "Timings can only exist together with audio content"
            }
        }
    }
}