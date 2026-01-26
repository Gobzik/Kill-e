package com.yourapp.infrastructure.persistence.repository

import com.yourapp.infrastructure.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * JPA Repository for UserEntity
 * Spring Data JPA will automatically implement this interface
 */
@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
}
