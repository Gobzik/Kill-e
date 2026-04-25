package com.kille.presentation.controller

import com.kille.application.port.output.StoragePort
import com.kille.config.S3Properties
import com.kille.domain.model.BookId
import com.kille.domain.model.ChapterId
import com.kille.domain.repository.ChapterRepository
import com.kille.presentation.dto.response.ApiResponse
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/books/{bookId}/chapters/{chapterId}/files")
class ChapterFileController(
    private val storagePort: StoragePort,
    private val chapterRepository: ChapterRepository,
    private val s3Properties: S3Properties
) {

    private fun loadChapter(bookId: UUID, chapterId: UUID) =
        chapterRepository.findById(ChapterId(chapterId))
            .orElseThrow {
                IllegalArgumentException("Chapter $chapterId not found")
            }
            .also { chapter ->
                require(chapter.bookId == BookId(bookId)) {
                    "Chapter $chapterId does not belong to book $bookId"
                }
            }

    private fun buildUploadKey(
        bookId: UUID,
        chapterId: UUID,
        file: MultipartFile,
        baseName: String
    ): String {
        val prefix = s3Properties.chaptersPrefix.trim().trim('/')
        val extension = file.originalFilename
            ?.substringAfterLast('.', "")
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) }
            ?.let { ".$it" }
            ?: ""

        return "$prefix/$bookId/$chapterId/$baseName-${System.currentTimeMillis()}$extension"
    }

    @PostMapping("/audio", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAudio(
        @RequestPart("file") file: MultipartFile,
        @PathVariable("bookId") bookId: UUID,
        @PathVariable("chapterId") chapterId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        require(!file.isEmpty) { "Audio file is empty" }
        val chapter = loadChapter(bookId, chapterId)

        val key = storagePort.uploadFile(buildUploadKey(bookId, chapterId, file, "audio"), file)
        val updatedChapter = chapter.addAudio(key, chapter.timingUrl)
        chapterRepository.save(updatedChapter)

        return ResponseEntity.ok(ApiResponse.success(key, "Audio uploaded successfully"))
    }

    @PostMapping("/text", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadText(
        @RequestPart("file") file: MultipartFile,
        @PathVariable("bookId") bookId: UUID,
        @PathVariable("chapterId") chapterId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        require(!file.isEmpty) { "Text file is empty" }
        val chapter = loadChapter(bookId, chapterId)

        val key = storagePort.uploadFile(buildUploadKey(bookId, chapterId, file, "text"), file)
        val updatedChapter = chapter.updateText(key)
        chapterRepository.save(updatedChapter)

        return ResponseEntity.ok(ApiResponse.success(key, "Text uploaded successfully"))
    }
}
