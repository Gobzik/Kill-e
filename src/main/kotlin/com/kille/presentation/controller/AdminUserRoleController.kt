package com.kille.presentation.controller

import com.kille.domain.repository.RoleRepository
import com.kille.domain.repository.UserRepository
import com.kille.presentation.dto.response.UserResponse
import com.kille.presentation.mapper.toResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminUserRoleController(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {
    @PostMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignAdminRole(@PathVariable id: UUID): UserResponse {
        val user = userRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val adminRole = roleRepository.findByName("ROLE_ADMIN")
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_ADMIN is not configured")

        if (user.roles.none { it.name == adminRole.name }) {
            user.roles.add(adminRole)
        }
        return userRepository.save(user).toResponse()
    }

    @DeleteMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun removeAdminRole(@PathVariable id: UUID): UserResponse {
        val user = userRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        user.roles.removeIf { it.name == "ROLE_ADMIN" }
        return userRepository.save(user).toResponse()
    }
}
