package com.yourapp.infrastructure.persistence.adapter

import com.yourapp.domain.model.User
import com.yourapp.domain.repository.UserRepository
import com.yourapp.infrastructure.persistence.entity.UserEntity
import com.yourapp.infrastructure.persistence.repository.UserJpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

/**
 * Adapter that implements domain UserRepository using JPA
 * Translates between domain models and JPA entities
 */
@Component
class UserRepositoryAdapter(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override suspend fun findById(id: Long): User? = withContext(Dispatchers.IO) {
        jpaRepository.findById(id).orElse(null)?.toDomain()
    }

    override suspend fun findAll(): List<User> = withContext(Dispatchers.IO) {
        jpaRepository.findAll().map { it.toDomain() }
    }

    override suspend fun findByUsername(username: String): User? = withContext(Dispatchers.IO) {
        jpaRepository.findByUsername(username)?.toDomain()
    }

    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        val entity = user.toEntity()
        jpaRepository.save(entity).toDomain()
    }

    override suspend fun deleteById(id: Long): Boolean = withContext(Dispatchers.IO) {
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id)
            true
        } else {
            false
        }
    }

    // Mapping functions
    private fun UserEntity.toDomain(): User = User(
        id = this.id,
        username = this.username,
        email = this.email,
        role = this.role
    )

    private fun User.toEntity(): UserEntity = UserEntity(
        id = this.id,
        username = this.username,
        email = this.email,
        role = this.role
    )
}
