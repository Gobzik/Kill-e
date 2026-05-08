package com.kille.presentation.dto.response

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val email: String?,
    val enabled: Boolean,
    val roles: List<String>
)