package com.kille.infrastructure.persistence.adapter

import com.kille.domain.model.Role
import com.kille.domain.repository.RoleRepository
import com.kille.infrastructure.persistence.entity.RoleEntity
import com.kille.infrastructure.persistence.repository.JpaRoleRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RoleRepositoryImpl(
    private val jpaRoleRepository: JpaRoleRepository
) : RoleRepository {

    override fun save(role: Role): Role {
        val entity = RoleEntity(id = role.id, name = role.name)
        val saved = jpaRoleRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): Role? =
        jpaRoleRepository.findById(id).orElse(null)?.toDomain()

    override fun findByName(name: String): Role? =
        jpaRoleRepository.findByName(name)?.toDomain()

    private fun RoleEntity.toDomain() = Role(id = id, name = name)
}