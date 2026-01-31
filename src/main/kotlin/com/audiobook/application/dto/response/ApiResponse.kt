package com.yourapp.application.dto.response

/**
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> error(error: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error
            )
        }
    }
}
