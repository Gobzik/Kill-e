package com.kille.infrastructure.persistence.repository

import com.kille.infrastructure.persistence.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaRoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: String): RoleEntity?
}