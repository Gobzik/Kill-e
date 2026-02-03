package com.yourapp.infrastructure.logging.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.logging")
data class LoggingProperties(
    val useCases: UseCaseLoggingConfig = UseCaseLoggingConfig(),
    val repositories: RepositoryLoggingConfig = RepositoryLoggingConfig(),
    val controllers: ControllerLoggingConfig = ControllerLoggingConfig()
) {
    data class UseCaseLoggingConfig(
        val enabled: Boolean = true,
        val includeArgs: Boolean = true,
        val includeResult: Boolean = true
    )

    data class RepositoryLoggingConfig(
        val enabled: Boolean = true,
        val includeArgs: Boolean = false,
        val includeResult: Boolean = false,
        val slowQueryThresholdMs: Long = 500
    )

    data class ControllerLoggingConfig(
        val enabled: Boolean = true,
        val includeHeaders: Boolean = false
    )
}
