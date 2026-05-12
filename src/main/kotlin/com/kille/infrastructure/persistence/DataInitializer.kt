package com.kille.infrastructure.persistence

import com.kille.domain.model.Role
import com.kille.domain.model.User
import com.kille.domain.repository.RoleRepository
import com.kille.domain.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.bootstrap.admin.email}")
    private val bootstrapAdminEmail: String,
    @Value("\${app.bootstrap.admin.username}")
    private val bootstrapAdminUsername: String,
    @Value("\${app.bootstrap.admin.password}")
    private val bootstrapAdminPassword: String
) {
    @Bean
    fun init() = CommandLineRunner {
        val defaultRoles = listOf("ROLE_USER", "ROLE_STANDARD", "ROLE_PREMIUM", "ROLE_ADMIN")
        defaultRoles.forEach { roleName ->
            if (roleRepository.findByName(roleName) == null) {
                roleRepository.save(Role(name = roleName))
            }
        }

        val hasAnyUsers = userRepository.existsAny()
        if (!hasAnyUsers) {
            val adminRole = roleRepository.findByName("ROLE_ADMIN")
                ?: roleRepository.save(Role(name = "ROLE_ADMIN"))
            val userRole = roleRepository.findByName("ROLE_USER")
                ?: roleRepository.save(Role(name = "ROLE_USER"))

            val admin = User(
                username = bootstrapAdminUsername,
                password = passwordEncoder.encode(bootstrapAdminPassword),
                email = bootstrapAdminEmail
            ).apply {
                roles.add(userRole)
                roles.add(adminRole)
            }
            userRepository.save(admin)
        }
    }
}