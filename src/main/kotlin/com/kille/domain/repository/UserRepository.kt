package com.kille.domain.repository

import com.kille.domain.model.User
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun existsAny(): Boolean
    fun delete(user: User)
}