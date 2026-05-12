package com.kille.presentation.controller

import com.kille.domain.model.User
import com.kille.domain.repository.RoleRepository
import com.kille.domain.repository.UserRepository
import com.kille.infrastructure.security.JwtService
import com.kille.presentation.dto.request.CreateUserRequest
import com.kille.presentation.dto.request.LoginRequest
import com.kille.presentation.dto.response.JwtResponse
import com.kille.presentation.dto.response.UserResponse
import com.kille.presentation.mapper.toResponse
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already exists")
        }
        val username = request.username?.takeIf { it.isNotBlank() } ?: request.email
        if (userRepository.findByUsername(username) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Username already exists")
        }
        val userRole = roleRepository.findByName("ROLE_USER")
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_USER is not configured")

        val user = User(
            username = username,
            password = passwordEncoder.encode(request.password),
            email = request.email
        ).apply { this.roles.add(userRole) }

        val saved = userRepository.save(user)
        return saved.toResponse()
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: UUID): UserResponse {
        val user = userRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return user.toResponse()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): JwtResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        if (!passwordEncoder.matches(request.password, user.password))
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        val token = jwtService.generateToken(
            username = user.username,
            roles = user.roles.map { it.name }
        )
        return JwtResponse(token = token)
    }
}