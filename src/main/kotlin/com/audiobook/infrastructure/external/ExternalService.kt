package com.audiobook.infrastructure.external

/**
 * This package is for external service integrations
 * Examples:
 * - External APIs
 * - Third-party services
 * - Message queues
 * - Email services
 * - etc.
 */

// Example external service interface
interface ExternalService {
    suspend fun callExternalApi(params: Map<String, Any>): Result<String>
}

// Example implementation would go here
