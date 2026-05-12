package com.kille.presentation.dto.request

data class CreateUserRequest(
    val email: String,
    val password: String,
    val username: String? = null
)