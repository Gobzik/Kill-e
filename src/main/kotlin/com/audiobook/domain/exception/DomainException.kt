package com.yourapp.domain.exception

/**
 * Base exception for domain layer
 */
open class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * Exception for when entity is not found
 */
class EntityNotFoundException(
    entityName: String,
    id: Any
) : DomainException("$entityName not found with id: $id")

/**
 * Exception for validation errors
 */
class ValidationException(
    message: String
) : DomainException(message)
