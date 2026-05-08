package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.Role
import com.kille.domain.model.User
import com.kille.domain.repository.UserRepository
import com.kille.infrastructure.persistence.entity.UserEntity
import com.kille.infrastructure.persistence.entity.UserRoleEntity
import com.kille.infrastructure.persistence.repository.JpaRoleRepository
import com.kille.infrastructure.persistence.repository.JpaUserRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository,
    private val jpaRoleRepository: JpaRoleRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = user.toEntity()
        val savedEntity = jpaUserRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): User? =
        jpaUserRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUsername(username: String): User? =
        jpaUserRepository.findByUsername(username)?.toDomain()

    override fun findByEmail(email: String): User? =
        jpaUserRepository.findByEmail(email)?.toDomain()

    override fun existsAny(): Boolean = jpaUserRepository.count() > 0

    override fun delete(user: User) {
        user.id?.let { jpaUserRepository.deleteById(it) }
    }

    private fun User.toEntity(): UserEntity {
        val entity = UserEntity(
            id = id,
            username = username,
            password = password,
            email = email,
            enabled = enabled
        )
        val userRoles = roles.mapNotNull { role ->
            val roleEntity = jpaRoleRepository.findByName(role.name) ?: return@mapNotNull null
            UserRoleEntity(user = entity, role = roleEntity)
        }.toMutableList()

        entity.roles.addAll(userRoles)
        return entity
    }

    private fun UserEntity.toDomain(): User = User(
        id = id,
        username = username,
        password = password,
        email = email,
        enabled = enabled,
        roles = roles.map { Role(id = it.role.id, name = it.role.name) }.toMutableList()
    )
}