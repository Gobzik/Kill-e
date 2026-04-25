package com.kille.presentation.exception

import com.kille.infrastructure.storage.exception.StorageOperationException
import com.kille.presentation.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException
import org.springframework.web.multipart.support.MissingServletRequestPartException

@RestControllerAdvice(basePackages = ["com.kille.presentation.controller"])
class GlobalExceptionHandler {

    private fun findStorageCause(ex: Throwable): StorageOperationException? {
        var current: Throwable? = ex
        while (current != null) {
            if (current is StorageOperationException) {
                return current
            }
            current = current.cause
        }
        return null
    }

    @ExceptionHandler(
        MissingServletRequestPartException::class,
        MissingServletRequestParameterException::class,
        MultipartException::class
    )
    fun handleMultipartErrors(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        val message = ex.message
            ?: "Invalid multipart request. Use -F 'file=@/path/to/file.txt' and do not set Content-Type manually."
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(ex: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<Unit>> {
        val message = ex.message
            ?: "Uploaded file is too large. Increase request limits or upload a smaller file."
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponse.error(message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "Invalid request"))
    }

    @ExceptionHandler(StorageOperationException::class)
    fun handleStorageError(ex: StorageOperationException): ResponseEntity<ApiResponse<Unit>> {
        val message = ex.message
            ?: "Storage upload failed. Check S3 credentials/bucket configuration and try again."
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ApiResponse.error(message))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleWrappedRuntime(ex: RuntimeException): ResponseEntity<ApiResponse<Unit>> {
        val storageCause = findStorageCause(ex)
        return if (storageCause != null) {
            handleStorageError(storageCause)
        } else {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.message ?: "Internal server error"))
        }
    }
}



