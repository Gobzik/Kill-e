package com.kille.presentation.controller

import com.kille.presentation.dto.request.CreateChapterRequest
import com.kille.presentation.dto.request.GenerateTimingsRequest
import com.kille.presentation.dto.response.ApiResponse
import com.kille.presentation.dto.response.ChapterResponse
import com.kille.application.port.input.chapter.CreateChapterUseCase
import com.kille.application.port.input.chapter.GenerateChapterTimingsCommand
import com.kille.application.port.input.chapter.GenerateChapterTimingsResult
import com.kille.application.port.input.chapter.GenerateChapterTimingsUseCase
import com.kille.application.port.input.chapter.GetPlayableChaptersUseCase
import com.kille.application.port.input.chapter.GetChapterUseCase
import com.kille.application.port.input.chapter.UpdateChapterUseCase
import com.kille.application.port.input.chapter.UpdateChapterCommand
import com.kille.application.port.input.chapter.DeleteChapterUseCase
import com.kille.domain.model.BookId
import com.kille.domain.model.ChapterIndex
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/chapters")
@Tag(name = "Chapters", description = "API для управления главами")
class ChapterController(
    private val createChapterUseCase: CreateChapterUseCase,
    private val generateChapterTimingsUseCase: GenerateChapterTimingsUseCase,
    private val getPlayableChaptersUseCase: GetPlayableChaptersUseCase,
    private val getChapterUseCase: GetChapterUseCase,
    private val updateChapterUseCase: UpdateChapterUseCase,
    private val deleteChapterUseCase: DeleteChapterUseCase
) {

    @PostMapping
    @Operation(summary = "Создать главу", description = "Создаёт новую главу")
    fun createChapter(@Valid @RequestBody request: CreateChapterRequest): ResponseEntity<ApiResponse<ChapterResponse>> {
        if (!request.validate()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Chapter must have either text or audio"))
        }

        val chapter = createChapterUseCase.execute(
            bookId = BookId.fromString(request.bookId),
            index = ChapterIndex(request.index),
            title = request.title,
            text = request.text,
            audioUrl = request.audioUrl,
            timingUrl = request.timingUrl
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(ChapterResponse.fromDomain(chapter), "Глава успешно создана"))
    }

    @PostMapping("/{id}/timings")
    @Operation(summary = "Сгенерировать тайминги", description = "Генерирует тайминги слов из аудио и сохраняет timings.json в S3")
    fun generateTimings(
        @PathVariable id: String,
        @RequestBody(required = false) request: GenerateTimingsRequest?
    ): ResponseEntity<ApiResponse<GenerateChapterTimingsResult>> {
        val result = generateChapterTimingsUseCase.execute(
            GenerateChapterTimingsCommand(
                chapterId = id,
                audioUrl = request?.audioUrl
            )
        )

        return ResponseEntity.ok(ApiResponse.success(result, "Тайминги успешно сгенерированы"))
    }

    @GetMapping("/book/{bookId}/playable")
    @Operation(summary = "Получить воспроизводимые главы", description = "Возвращает все воспроизводимые главы книги")
    fun getPlayableChapters(@PathVariable bookId: String): ResponseEntity<ApiResponse<List<ChapterResponse>>> {
        val chapters = getPlayableChaptersUseCase.execute(BookId.fromString(bookId))
        return ResponseEntity.ok(ApiResponse.success(chapters.map { ChapterResponse.fromDomain(it) }))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить главу по ID", description = "Возвращает главу")
    fun getChapter(@PathVariable id: String): ResponseEntity<ApiResponse<ChapterResponse>> {
        val result = getChapterUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить главу", description = "Обновляет существующую главу")
    fun updateChapter(
        @PathVariable id: String,
        @RequestBody request: UpdateChapterRequest
    ): ResponseEntity<ApiResponse<ChapterResponse>> {
        val command = UpdateChapterCommand(
            chapterId = id,
            text = request.text,
            audioUrl = request.audioUrl,
            timingUrl = request.timingUrl
        )
        val result = updateChapterUseCase.execute(command)
        return ResponseEntity.ok(ApiResponse.success(result, "Глава успешно обновлена"))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить главу", description = "Удаляет главу")
    fun deleteChapter(@PathVariable id: String): ResponseEntity<ApiResponse<Unit>> {
        deleteChapterUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Глава успешно удалена"))
    }
}

data class UpdateChapterRequest(
    val text: String? = null,
    val audioUrl: String? = null,
    val timingUrl: String? = null
)
