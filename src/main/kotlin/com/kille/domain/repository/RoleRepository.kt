package com.kille.domain.repository

import com.kille.domain.model.Role
import java.util.UUID

interface RoleRepository {
    fun save(role: Role): Role
    fun findById(id: UUID): Role?
    fun findByName(name: String): Role?
}