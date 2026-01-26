package com.yourapp.di

import com.yourapp.domain.repository.UserRepository
import com.yourapp.infrastructure.persistence.adapter.UserRepositoryAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Dependency Injection configuration
 * Configure beans and wire dependencies
 */
@Configuration
class BeanConfiguration {

    /**
     * Provide UserRepository implementation
     * This allows the domain layer to depend on abstractions
     */
    @Bean
    fun userRepository(adapter: UserRepositoryAdapter): UserRepository {
        return adapter
    }

    // Additional beans can be configured here
    // For example: external services, custom validators, etc.
}
