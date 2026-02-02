package com.yourapp.presentation.controller

import com.yourapp.application.dto.request.CreateExampleRequest
import com.yourapp.application.dto.response.ApiResponse
import com.yourapp.application.dto.response.ExampleResponse
import com.yourapp.application.usecase.example.CreateExampleUseCase
import com.yourapp.application.usecase.example.GetAllExamplesUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller для ExampleEntity.
 *
 * Presentation Layer - координирует HTTP запросы и Use Cases.
 * Не содержит бизнес-логики!
 */
@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Examples", description = "API для демонстрации ExampleEntity (DDD)")
class ExampleController(
    private val createExampleUseCase: CreateExampleUseCase,
    private val getAllExamplesUseCase: GetAllExamplesUseCase
) {

    /**
     * GET /api/v1/examples
     * Получение всех примеров.
     */
    @GetMapping
    @Operation(summary = "Получить все ExampleEntity", description = "Возвращает список всех задач")
    fun getAll(): ResponseEntity<ApiResponse<List<ExampleResponse>>> {
        val result = getAllExamplesUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    /**
     * POST /api/v1/examples
     * Создание нового примера.
     */
    @PostMapping
    @Operation(summary = "Создать ExampleEntity", description = "Создаёт новую задачу со статусом TODO")
    fun create(
        @Valid @RequestBody request: CreateExampleRequest
    ): ResponseEntity<ApiResponse<ExampleResponse>> {
        val result = createExampleUseCase.execute(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(result))
    }
}
