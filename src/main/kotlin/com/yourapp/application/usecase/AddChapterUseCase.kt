package com.yourapp.application.usecase.book

import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.UseCase
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Use Case: Добавление главы в книгу.
 */
@Service
class AddChapterUseCase(
    private val repository: BookRepository
) : UseCase<AddChapterCommand, ChapterResponse> {

    @Transactional
    override fun execute(command: AddChapterCommand): ChapterResponse {
        // 1. Находим книгу из репозитория
        val book = repository.findById(BookId(command.bookId))
            ?: throw RuntimeException("Book with ID ${command.bookId} not found")

        // 2. Создаём главу
        val chapter = Chapter.create(
            title = command.title,
            content = command.content,
            index = command.index,
            audioUrl = command.audioUrl,
            bookId = BookId(command.bookId)
        )

        // 3. Добавляем главу в книгу (доменная логика)
        book.addChapter(chapter)

        // 4. Сохраняем книгу обратно
        repository.save(book)

        // 5. Возвращаем DTO главы
        return ChapterResponse(
            id = chapter.id.value,
            title = chapter.title,
            index = chapter.index,
            hasAudio = chapter.audioUrl != null,
            hasText = chapter.content.isNotBlank()
        )
    }
}

/**
 * Команда для добавления главы.
 */
data class AddChapterCommand(
    val bookId: UUID,
    val title: String,
    val content: String,
    val index: Int,
    val audioUrl: String? = null
)