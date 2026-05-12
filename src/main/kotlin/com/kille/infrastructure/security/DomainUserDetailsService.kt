package com.kille.infrastructure.security

import com.kille.domain.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class DomainUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User $username not found")
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            user.enabled,
            true, true, true,
            user.roles.map { SimpleGrantedAuthority(it.name) }
        )
    }
}