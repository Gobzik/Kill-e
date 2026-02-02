package com.yourapp.presentation.mapper

import com.yourapp.domain.model.Book
import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.dto.response.ChapterResponse
import org.springframework.stereotype.Component

@Component
class BookMapper {

    fun toResponse(book: Book): BookResponse {
        return BookResponse(
            id = book.id.value,
            title = book.title,
            author = book.author,
            language = book.language,
            coverUrl = book.coverUrl,
            hasAudio = book.hasAudio(),
            hasText = book.hasText(),
            chapterCount = book.chapterCount(),
            chapters = book.chapters().map { ChapterResponse.fromDomain(it) }
        )
    }
}