package com.yourapp.domain.exception

open class DomainException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class EntityNotFoundException(
    entityName: String,
    id: Any
) : DomainException("$entityName not found with id: $id")

class ValidationException(
    message: String
) : DomainException(message)
