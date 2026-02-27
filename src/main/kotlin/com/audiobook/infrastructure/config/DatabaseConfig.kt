package com.audiobook.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Database configuration
 * Configure JPA repositories and transaction management
 */
@Configuration
@EnableJpaRepositories(basePackages = ["com.audiobook.infrastructure.persistence.repository"])
@EnableTransactionManagement
class DatabaseConfig {
    // Additional database configurations can be added here
    // For example: DataSource beans, connection pool settings, etc.
}
