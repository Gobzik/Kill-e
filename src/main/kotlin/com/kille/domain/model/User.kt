package com.kille.domain.model

import java.util.UUID

data class User(
    val id: UUID? = null,
    val username: String,
    val password: String,
    val email: String? = null,
    val enabled: Boolean = true,
    val roles: MutableList<Role> = mutableListOf()
)
