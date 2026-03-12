package com.kille.infrastructure.storage.dto

data class ChapterFiles(
    val audioUrl: String?,
    val textContent: String?,
    val timingsContent: String?
)

data class FileUploadResult(
    val audioKey: String?,
    val textKey: String?,
    val timingsKey: String?
)

enum class FileType {
    AUDIO,
    TEXT,
    TIMINGS
}