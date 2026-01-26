package com.yourapp.application.dto.response

import com.yourapp.domain.model.Role

/**
 * DTO for user response
 */
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: Role
)
