package com.audiobook.presentation.controller

import com.audiobook.application.dto.request.CreateChapterRequest
import com.audiobook.application.dto.response.ChapterResponse
import com.audiobook.application.usecase.chapter.CreateChapterUseCase
import com.audiobook.application.usecase.chapter.GetPlayableChaptersUseCase
import com.audiobook.domain.model.BookId
import com.audiobook.domain.model.ChapterIndex
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/chapters")
class ChapterController(
    private val createChapterUseCase: CreateChapterUseCase,
    private val getPlayableChaptersUseCase: GetPlayableChaptersUseCase
) {

    @PostMapping
    fun createChapter(@Valid @RequestBody request: CreateChapterRequest): ResponseEntity<ChapterResponse> {
        if (!request.validate()) {
            return ResponseEntity.badRequest().body(null)
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
            .body(ChapterResponse.fromDomain(chapter))
    }

    @GetMapping("/book/{bookId}/playable")
    fun getPlayableChapters(@PathVariable bookId: String): ResponseEntity<List<ChapterResponse>> {
        val chapters = getPlayableChaptersUseCase.execute(BookId.fromString(bookId))
        return ResponseEntity.ok(chapters.map { ChapterResponse.fromDomain(it) })
    }

    @GetMapping("/{id}")
    fun getChapter(@PathVariable id: String): ResponseEntity<ChapterResponse> {
        // TODO: Implement
        return ResponseEntity.notFound().build()
    }
}