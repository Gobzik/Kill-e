package com.kille.application.port.output

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID

interface StoragePort {
    fun uploadAudio(bookId: UUID, chapterId: UUID, file: MultipartFile): String
    fun uploadText(bookId: UUID, chapterId: UUID, content: String): String
    fun uploadTimings(bookId: UUID, chapterId: UUID, timings: String): String

    fun getAudioUrl(bookId: UUID, chapterId: UUID): String?
    fun getText(bookId: UUID, chapterId: UUID): String?
    fun getTimings(bookId: UUID, chapterId: UUID): String?

    fun deleteChapterFiles(bookId: UUID, chapterId: UUID)
    fun chapterExists(bookId: UUID, chapterId: UUID): Boolean

    fun getAudioInputStream(bookId: UUID, chapterId: UUID): InputStream?
}