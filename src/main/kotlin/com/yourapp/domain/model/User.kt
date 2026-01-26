package com.yourapp.domain.model

/**
 * Domain model for User
 * This is pure Kotlin, no framework dependencies
 */
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val role: Role
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(email.contains("@")) { "Email must be valid" }
    }
}
