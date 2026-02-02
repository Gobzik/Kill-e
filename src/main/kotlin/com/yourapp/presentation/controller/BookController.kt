package com.yourapp.presentation.controller

import com.yourapp.application.dto.request.CreateBookRequest
import com.yourapp.application.dto.request.AddChapterRequest
import com.yourapp.application.dto.request.UpdateBookRequest
import com.yourapp.application.dto.response.ApiResponse
import com.yourapp.application.dto.response.BookResponse
import com.yourapp.application.dto.response.ChapterResponse
import com.yourapp.application.usecase.book.CreateBookUseCase
import com.yourapp.application.usecase.book.GetAllBooksUseCase
import com.yourapp.application.usecase.book.GetBookUseCase
import com.yourapp.application.usecase.book.AddChapterUseCase
import com.yourapp.application.usecase.book.UpdateBookUseCase
import com.yourapp.application.usecase.book.UpdateBookCommand
import com.yourapp.application.usecase.book.DeleteBookUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "API для управления книгами и главами")
class BookController(
    private val createBookUseCase: CreateBookUseCase,
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val getBookUseCase: GetBookUseCase,
    private val addChapterUseCase: AddChapterUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase
) {

    @GetMapping
    @Operation(summary = "Получить все книги", description = "Возвращает список всех книг")
    fun getAll(): ResponseEntity<ApiResponse<List<BookResponse>>> {
        val result = getAllBooksUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID", description = "Возвращает книгу с главами")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<BookResponse>> {
        val result = getBookUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PostMapping
    @Operation(summary = "Создать книгу", description = "Создаёт новую книгу с главами")
    fun create(
        @Valid @RequestBody request: CreateBookRequest
    ): ResponseEntity<ApiResponse<BookResponse>> {
        val result = createBookUseCase.execute(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "Книга успешно создана"))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить книгу", description = "Обновляет существующую книгу")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateBookRequest
    ): ResponseEntity<ApiResponse<BookResponse>> {
        val command = UpdateBookCommand(
            bookId = id,
            title = request.title,
            author = request.author,
            language = request.language,
            coverUrl = request.coverUrl
        )
        val result = updateBookUseCase.execute(command)
        return ResponseEntity.ok(ApiResponse.success(result, "Книга успешно обновлена"))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу", description = "Удаляет книгу и все её главы")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        deleteBookUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Книга успешно удалена"))
    }

    @PostMapping("/{bookId}/chapters")
    @Operation(summary = "Добавить главу", description = "Добавляет новую главу в существующую книгу")
    fun addChapter(
        @PathVariable bookId: UUID,
        @Valid @RequestBody request: AddChapterRequest
    ): ResponseEntity<ApiResponse<ChapterResponse>> {
        val command = com.yourapp.application.usecase.book.AddChapterCommand(
            bookId = bookId,
            title = request.title,
            content = request.content,
            index = request.index,
            audioUrl = request.audioUrl
        )
        val result = addChapterUseCase.execute(command)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "Глава успешно добавлена"))
    }
}