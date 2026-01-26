package com.yourapp.infrastructure.persistence.entity

import com.yourapp.domain.model.Role
import jakarta.persistence.*

/**
 * JPA Entity for User
 * This is the database representation of User
 */
@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role
)
