package com.kille.presentation.controller

import com.kille.presentation.dto.request.CreateBookRequest
import com.kille.presentation.dto.request.UpdateBookRequest
import com.kille.presentation.dto.response.ApiResponse
import com.kille.presentation.dto.response.BookResponse
import com.kille.application.port.input.book.CreateBookUseCase
import com.kille.application.port.input.book.GetAllBooksUseCase
import com.kille.application.port.input.book.GetBookUseCase
import com.kille.application.port.input.book.UpdateBookUseCase
import com.kille.application.port.input.book.UpdateBookCommand
import com.kille.application.port.input.book.DeleteBookUseCase
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

    @PostMapping(produces = ["application/json"])
    @Operation(summary = "Создать книгу", description = "Создаёт новую книгу (все поля передаются как параметры)")
    fun create(
        @RequestParam title: String,
        @RequestParam author: String,
        @RequestParam language: String
    ): ResponseEntity<ApiResponse<BookResponse>> {
        val request = CreateBookRequest(
            title = title.trim(),
            author = author.trim(),
            language = language.trim(),
            coverUrl = null,
            audio = false,
            text = true,
            chapters = emptyList()
        )

        val result = createBookUseCase.execute(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "Книга успешно создана"))
    }

    @RequestMapping(value = ["/{id}"], method = [RequestMethod.PUT, RequestMethod.PATCH])
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
}