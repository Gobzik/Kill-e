package com.yourapp.domain.repository

import com.yourapp.domain.model.User

/**
 * Repository interface for User domain model
 * Pure Kotlin interface with no framework dependencies
 */
interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findAll(): List<User>
    suspend fun findByUsername(username: String): User?
    suspend fun save(user: User): User
    suspend fun deleteById(id: Long): Boolean
}
