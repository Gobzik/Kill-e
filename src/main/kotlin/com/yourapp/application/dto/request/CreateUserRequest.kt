package com.yourapp.application.dto.request

import com.yourapp.domain.model.Role

/**
 * DTO for creating a new user
 */
data class CreateUserRequest(
    val username: String,
    val email: String,
    val role: Role = Role.USER
)
