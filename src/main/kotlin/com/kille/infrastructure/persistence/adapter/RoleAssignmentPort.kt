package com.kille.infrastructure.persistence.adapter

import com.kille.application.port.output.RoleAssignmentPort
import com.kille.domain.repository.RoleRepository
import com.kille.domain.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class RoleAssignmentAdapter(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) : RoleAssignmentPort {

    @Transactional
    override fun assignSubscriptionRole(userId: UUID, plan: String) {
        val user = userRepository.findById(userId) ?: throw NoSuchElementException("User not found")
        val roleName = when (plan.lowercase()) {
            "standard" -> "ROLE_STANDARD"
            "premium" -> "ROLE_PREMIUM"
            else -> throw IllegalArgumentException("Unsupported plan: $plan")
        }
        val role = roleRepository.findByName(roleName) ?: throw NoSuchElementException("Role $roleName not found")
        user.roles.removeIf { it.name == "ROLE_STANDARD" || it.name == "ROLE_PREMIUM" }
        user.roles.add(role)
        userRepository.save(user)
    }

    @Transactional
    override fun clearSubscriptionRoles(userId: UUID) {
        val user = userRepository.findById(userId) ?: return
        val subscriptionRoles = listOf("ROLE_STANDARD", "ROLE_PREMIUM")
        user.roles.removeAll { it.name in subscriptionRoles }
        userRepository.save(user)
    }
}