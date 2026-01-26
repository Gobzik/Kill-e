package com.yourapp.application.dto.request

import com.yourapp.domain.model.Role

/**
 * DTO for updating existing user
 */
data class UpdateUserRequest(
    val id: Long,
    val username: String? = null,
    val email: String? = null,
    val role: Role? = null
)
