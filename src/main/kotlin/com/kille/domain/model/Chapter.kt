package com.kille.domain.model

import com.kille.domain.exception.DomainException
import java.time.LocalDateTime

@ConsistentCopyVisibility
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

    private fun isPlaceholderMediaValue(value: String?): Boolean {
        val normalized = value?.trim()?.lowercase() ?: return false
        return normalized == "string" || normalized == "null" || normalized == "undefined"
    }

    val index: ChapterIndex get() = _index
    val title: String? get() = _title
    val text: String? get() = _text
    val audioUrl: String? get() = _audioUrl?.takeUnless { isPlaceholderMediaValue(it) }
    val timingUrl: String? get() = _timingUrl?.takeUnless { isPlaceholderMediaValue(it) }
    val updatedAt: LocalDateTime get() = _updatedAt
    val durationMs: Long? get() = _durationMs

    companion object {
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

    fun hasText(): Boolean = !_text.isNullOrBlank()
    fun hasAudio(): Boolean = !audioUrl.isNullOrBlank()
    fun hasTimings(): Boolean = !timingUrl.isNullOrBlank()
    fun isPlayable(): Boolean = hasText() || hasAudio()
    fun durationAvailable(): Boolean = _durationMs != null || hasTimings()

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

}