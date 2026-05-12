package com.kille.presentation.mapper

import com.kille.domain.model.Book
import com.kille.presentation.dto.response.BookResponse
import com.kille.presentation.dto.response.ChapterResponse
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
            chapterCount = book.chapterCount(),
            chapters = book.chapters().map { ChapterResponse.fromDomain(it) }
        )
    }
}