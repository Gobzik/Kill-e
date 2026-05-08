package com.kille.presentation.mapper

import com.kille.domain.model.User
import com.kille.presentation.dto.response.UserResponse

fun User.toResponse() = UserResponse(
    id = id ?: throw IllegalStateException("User without id"),
    username = username,
    email = email,
    enabled = enabled,
    roles = roles.map { it.name }
)